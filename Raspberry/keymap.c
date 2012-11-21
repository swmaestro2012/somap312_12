#include "keymap.h"

// Function to find a windowid by name
Window FindWindow(const char* WindowName)
{
        char buf[MAX_BUF];
        FILE* fi;
        Window findedWindow = 0;

        memset(buf, 0x00, sizeof(buf));
        sprintf(buf, "xwininfo -root -all | grep \"%s\" > win.info", WindowName);
        system(buf);

        fi = fopen("win.info", "r");
        if(fi == NULL)
                return 0;

        while(!feof(fi))
        {
                memset(buf, 0x00, sizeof(buf));
                fgets(buf, MAX_BUF, fi);

                if(0 < strstr(buf, "calligrastage"))
                {
                        sscanf(buf, "%x", &findedWindow);

			// Change focus to viewer
			sprintf(buf, "xdotool windowfocus %#x", findedWindow);
			system(buf);
                }
        }
        fclose(fi);

        return findedWindow;
}

// Function to create a keyboard event
XKeyEvent createKeyEvent(Display *display, Window win,
                         Window winRoot, bool press,
                         int keycode, int modifiers)
{
	XKeyEvent event;

	event.display     = display;
	event.window      = win;
	event.root        = winRoot;
	event.subwindow   = None;
	event.time        = CurrentTime;
	event.x           = 1;
	event.y           = 1;
	event.x_root      = 1;
	event.y_root      = 1;
	event.same_screen = True;
	event.keycode     = XKeysymToKeycode(display, keycode);
	event.state       = modifiers;
	if(press)
		event.type = KeyPress;
	else
		event.type = KeyRelease;

	return event;
}
int MakeDisplay(Display **display, Window *winRoot)
{
	// Obtain the X11 display.
	*display = XOpenDisplay(0);
	if(*display == NULL)
		return 0;

	// Get the root window for the current display.
	*winRoot = XDefaultRootWindow(*display);

	return 1;
}
void KeyProcessing(Display *display, const Window winRoot, const Window windowID, int keyType)
{
	XKeyEvent event;

	// Send a fake key press event to the window.
	printf("[createKeyEvent] \n");
printf("%x %x %x \n", display, windowID, winRoot);
	event = createKeyEvent(display, windowID, winRoot, 1, keyType, 0);
	XSendEvent(event.display, event.window, True, KeyPressMask, (XEvent *)&event);
	printf("[XSendEvent] \n");
	printf("event.window = %lx \n", (unsigned long)event.window);
        printf("KeyPressMask = %ld \n", KeyPressMask);
        printf("winFocus = %lx \n", (unsigned long)windowID);


	// Send a fake key release event to the window.
	event = createKeyEvent(display, windowID, winRoot, 0, keyType, 0);
	XSendEvent(event.display, event.window, True, KeyPressMask, (XEvent *)&event);
}


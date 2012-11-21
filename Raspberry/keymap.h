#ifndef _KEYMAP_H_
#define _KEYMAP_H_

#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <unistd.h>
#include <X11/Xlib.h>
#include <X11/keysym.h>

#define MAX_BUF 255

// The key code to be sent.
// A full list of available codes can be found in /usr/include/X11/keysymdef.h
#define KEY_F5		XK_F5
#define KEY_LEFT	XK_Left
#define KEY_RIGHT	XK_Right

Window FindWindow(const char* WindowName);
XKeyEvent createKeyEvent(Display *display, Window win, Window winRoot, bool press, int keycode, int modifiers);
int MakeDisplay(Display **display, Window *winRoot);
void KeyProcessing(Display *display, const Window winRoot, const Window windowID, int keyType);

#endif

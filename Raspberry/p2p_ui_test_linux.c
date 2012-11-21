#include "p2p_test.h"

char *naming_wpsinfo(int wps_info)
{
		switch(wps_info)
		{
				case P2P_NO_WPSINFO: return ("P2P_NO_WPSINFO");
				case P2P_GOT_WPSINFO_PEER_DISPLAY_PIN: return ("P2P_GOT_WPSINFO_PEER_DISPLAY_PIN");
				case P2P_GOT_WPSINFO_SELF_DISPLAY_PIN: return ("P2P_GOT_WPSINFO_SELF_DISPLAY_PIN");
				case P2P_GOT_WPSINFO_PBC: return ("P2P_GOT_WPSINFO_PBC");
				default: return ("UI unknown failed");
		}
}

char *naming_role(int role)
{
		switch(role)
		{
				case P2P_ROLE_DISABLE: return ("P2P_ROLE_DISABLE");
				case P2P_ROLE_DEVICE: return ("P2P_ROLE_DEVICE");
				case P2P_ROLE_CLIENT: return ("P2P_ROLE_CLIENT");
				case P2P_ROLE_GO: return ("P2P_ROLE_GO");
				default: return ("UI unknown failed");
		}
}

char *naming_status(int status)
{
		switch(status)
		{
				case P2P_STATE_NONE: return ("P2P_STATE_NONE");
				case P2P_STATE_IDLE: return ("P2P_STATE_IDLE");
				case P2P_STATE_LISTEN: return ("P2P_STATE_LISTEN");
				case P2P_STATE_SCAN: return ("P2P_STATE_SCAN");
				case P2P_STATE_FIND_PHASE_LISTEN: return ("P2P_STATE_FIND_PHASE_LISTEN");
				case P2P_STATE_FIND_PHASE_SEARCH: return ("P2P_STATE_FIND_PHASE_SEARCH");
				case P2P_STATE_TX_PROVISION_DIS_REQ: return ("P2P_STATE_TX_PROVISION_DIS_REQ");
				case P2P_STATE_RX_PROVISION_DIS_RSP: return ("P2P_STATE_RX_PROVISION_DIS_RSP");
				case P2P_STATE_RX_PROVISION_DIS_REQ: return ("P2P_STATE_RX_PROVISION_DIS_REQ");
				case P2P_STATE_GONEGO_ING: return ("P2P_STATE_GONEGO_ING");
				case P2P_STATE_GONEGO_OK: return ("P2P_STATE_GONEGO_OK");
				case P2P_STATE_GONEGO_FAIL: return ("P2P_STATE_GONEGO_FAIL");
				case P2P_STATE_RECV_INVITE_REQ: return ("P2P_STATE_RECV_INVITE_REQ");
				case P2P_STATE_PROVISIONING_ING: return ("P2P_STATE_PROVISIONING_ING");
				case P2P_STATE_PROVISIONING_DONE: return ("P2P_STATE_PROVISIONING_DONE");
				default: return ("UI unknown failed");
		}
}

char* naming_enable(int enable)
{
		switch(enable)
		{
				case  P2P_ROLE_DISABLE: return ("[Disabled]");
				case  P2P_ROLE_DEVICE: return ("[Enable/Device]");
				case  P2P_ROLE_CLIENT: return ("[Enable/Client]");
				case  P2P_ROLE_GO: return ("[Enable/GO]");
				default: return ("UI unknown failed");
		}
}

void ui_screen(struct p2p *p)
{
		system("clear");
		if(p->ap_open == _TRUE) 
		{
				printf("p->ap_open == TRUE\n");
		}
		else
		{
				printf("p->ap_open == FALSE\n");
		}
		if(p->wpa_open == _TRUE)
		{
				printf("p->wpa_open == TRUE\n");
		}
		else
		{
				printf("p->wpa_open == FLASE\n");
		}
		if(p->wpsing == _TRUE)
		{
				printf("p->wpsing == TRUE\n");
		}
		else
		{
				printf("p->wpsing == FALSE\n");
		}

		printf("*%-98s*\n", p->print_line);
}

void init_p2p(struct p2p *p)
{
		strcpy( p->ifname, "wlan0" );
		p->enable = P2P_ROLE_DISABLE;
		p->res = 1;
		p->res_go = 1;
		p->status = P2P_STATE_NONE;
		p->intent = 1;
		p->wps_info = 0;
		p->wpsing = 0;
		p->pin = 12345670;
		p->role = P2P_ROLE_DISABLE;
		p->listen_ch = 11;
		strcpy( p->peer_devaddr, "00:00:00:00:00:00" );
		p->p2p_get = 0;
		memset( p->print_line, 0x00, CMD_SZ);
		p->have_p2p_dev = 0;
		p->count_line = 0;
		strcpy( p->peer_ifaddr, "00:00:00:00:00:00" );
		memset( p->cmd, 0x00, CMD_SZ);
		p->wpa_open=0;
		p->ap_open=0;
		strcpy(p->ok_msg, "WiFi Direct handshake done" );
		strcpy(p->redo_msg, "Re-do GO handshake" );
		strcpy(p->fail_msg, "GO handshake unsuccessful" );
		strcpy(p->nego_msg, "Start P2P negotiation" );
		strcpy(p->wpa_conf, "./wpa_0_8.conf" );
		strcpy(p->wpa_path, "./wpa_supplicant" );
		strcpy(p->wpacli_path, "./wpa_cli" );
		strcpy(p->ap_conf, "./p2p_hostapd.conf" );
		strcpy(p->ap_path, "./hostapd" );
		strcpy(p->apcli_path, "./hostapd_cli" );
		strcpy(p->scan_msg, "Device haven't enable p2p functionalities" );

}

void rename_intf(struct p2p *p)
{
		FILE *pfin = NULL;
		FILE *pfout = NULL;

		pfin = fopen( p->ap_conf, "r" );
		pfout = fopen( "./p2p_hostapd_temp.conf", "w" );

		if ( pfin )
		{
				while( !feof( pfin ) ){
						memset(p->parse, 0x00, CMD_SZ);
						fgets(p->parse, CMD_SZ, pfin);

						if(strncmp(p->parse, "interface=", 10) == 0)
						{
								memset(p->parse, 0x00, CMD_SZ);
								sprintf( p->parse, "interface=%s\n", p->ifname );
								fputs( p->parse, pfout );
						}
						else
								fputs(p->parse, pfout);
				}
		}

		fclose( pfout );

		system( "rm -rf ./p2p_hostapd.conf" );
		system( "mv ./p2p_hostapd_temp.conf ./p2p_hostapd.conf" );

		return;
}

//int main()
int main(int argc, char **argv)
{
		char	peerifa[40] = { 0x00 };
		char	scan[CMD_SZ];
		struct p2p p2pstruct;
		struct p2p *p=NULL;

		p = &p2pstruct;
		if( p != NULL)
		{
				memset(p, 0x00, sizeof(struct p2p));
				init_p2p(p);
		}

		strcpy(p->ifname, argv[1] );
	
	system("rm wpa_0_8.conf");
	system("cp wpa_0_8.conf.backup wpa_0_8.conf");

ui_screen(p);
		/* Disable P2P functionalities at first*/
		p->enable=P2P_ROLE_DISABLE;
		p2p_enable(p);
		p2p_get_hostapd_conf(p);
		usleep(1 * SEC);

		rename_intf(p);

ui_screen(p);
		if( p->thread_trigger == THREAD_NONE )		//Active mode for user interface 
		{
				/* command:e, 1 */
				p->enable = P2P_ROLE_DEVICE;
				p2p_enable(p);
				usleep(1*SEC);
				
ui_screen(p);

				/* command:i, 15 */
				p->intent = 1;
				p2p_intent(p);
				usleep(0.5*SEC);

ui_screen(p);

				/* command:w, 3 */
				p->wps_info = 3;
				p2p_wpsinfo(p);
				usleep(0.5*SEC);

ui_screen(p);

				/* command:d, SMProjector */
#define DEVICE_NAME "SMProjector"
				p->p2p_get = 0;
				strncpy(p->dev_name, DEVICE_NAME, sizeof(DEVICE_NAME));
				p2p_setDN(p);
				usleep(0.5*SEC);

ui_screen(p);

				/* command:t, SMProjector */
#define SSID "SMProjector_SSID"
				strncpy(p->apd_ssid, SSID, sizeof(SSID));
				p2p_softap_ssid(p, "", 0);
				p->show_scan_result = 1;
				usleep(0.5*SEC);
ui_screen(p);
		}
		p->res=0;
		while( p->res == 0 ){

				p2p_status(p, 0);
				if( (p->status == P2P_STATE_RX_PROVISION_DIS_REQ) || (p->status == P2P_STATE_GONEGO_FAIL) )
				{
						printf("O");
						p->thread_trigger = THREAD_DEVICE ;

						char peer_devaddr[18];
						char peer_req_cm[4];

						memset( peer_devaddr, 0x00, 18);
						memset( peer_req_cm, 0x00, 4);

						p2p_peer_devaddr(p, peer_devaddr);
						p2p_peer_req_cm(p, peer_req_cm);
						p2p_peer_info(p, p->peer_devaddr, peer_req_cm);

						ui_screen(p);

						//strncpy(p->peer_devaddr, peer_devaddr, 17);
						if( (strncmp( peer_req_cm, "dis", 3) == 0) || (strncmp( peer_req_cm, "lab", 3) == 0) )
						{
								printf("Here is your PIN, insert c to continue: %d\n", p->pin);
						}
						else if( (strncmp( peer_req_cm, "pbc", 3) == 0) )
						{
								printf("Waitting to accept... \n");
								p->wps_info=3;
								p2p_wpsinfo(p);

								p2p_status(p, 0);

								if(p->status != P2P_STATE_GONEGO_OK)
								{
										p2p_set_nego(p);
								}
								else
								{
										p2p_role(p,0);

										if( p->role == P2P_ROLE_CLIENT )
										{
												p2p_client_mode(p);
										}
										else if( p->role == P2P_ROLE_GO )
										{
												p2p_go_mode(p);
										}
								}

								p2p_socket();

						}
						else if( (strncmp( peer_req_cm, "pad", 3) == 0) )
						{
								printf("Please insert peer PIN code:\n");
						}

						break;
				}

		}

		/* Disable P2P functionalities when exits*/
		p->enable= -1 ;
		p2p_enable(p);

		system( "rm -f ./supp_status.txt" );
		system( "rm -f ./temp.txt" );
		system( "rm -f ./scan.txt" );
		system( "rm -f ./peer.txt" );
		system( "rm -f ./status.txt" );
		system( "rm -f ./cm.txt" );

		return 0;

}

kill -9 `ps -ef | grep P2P_UI | grep -v grep | awk '{print $2}'`
kill -9 `ps -ef | grep iwpriv | grep -v grep | awk '{print $2}'`
kill -9 `ps -ef | grep wpa | grep -v grep | awk '{print $2}'`
ps aux | grep wlan0

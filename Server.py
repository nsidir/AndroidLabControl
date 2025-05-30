import socket
import platform
import threading
import os
import time

HOST = '0.0.0.0'
PORT = 41007

def get_hostname():
    return socket.gethostname()

def get_os():
    sys = platform.system()
    if sys == "Linux":
        return "Linux"
    elif sys == "Windows":
        return "Windows"
    else:
        return sys

def handle_echo(conn, hostname, os_name):
    print("Echo command received")
    response = f"{hostname} - {os_name}"
    conn.sendall(response.encode())
    conn.close()

def handle_restart(conn, hostname, os_name):
    print("Restart command received")
    response = f"{hostname} - Rebooting..."
    conn.sendall(response.encode())
    conn.close()
    # if os_name == "Linux":
    #     os.system("reboot")
    # elif os_name == "Windows":
    #     os.system("shutdown /r /t 0")

def handle_shutdown(conn, hostname, os_name):
    print("Shutdown command received")
    response = f"{hostname} - Shutting down..."
    conn.sendall(response.encode())
    conn.close()
    # if os_name == "Linux":
    #     os.system("shutdown now")
    # elif os_name == "Windows":
    #     os.system("shutdown /s /t 0")

def handle_restore(conn, hostname):
    print("Restore command received")
    try:
        print("Starting restore process...")
        start_msg = f"{hostname} - Restoring\n"
        conn.sendall(start_msg.encode())
        conn.send(b'\0')  # Send flush signal
        time.sleep(0.1)
        
        # Simulate restore process
        time.sleep(60)
        
        print("Restore finished")
        finish_msg = f"{hostname} - Restored\n"
        conn.sendall(finish_msg.encode())
        
    except Exception as e:
        print(f"Restore error: {e}")
    finally:
        conn.close()

def handle_client(conn, addr):
    hostname = get_hostname()
    os_name = get_os()
    
    try:
        data = conn.recv(1024).decode().strip()
        if not data:
            conn.close()
            return

        command = data.lower()
        
        if command == "echo":
            threading.Thread(
                target=handle_echo,
                args=(conn, hostname, os_name)
            ).start()
            
        elif command == "restart":
            threading.Thread(
                target=handle_restart,
                args=(conn, hostname, os_name)
            ).start()
            
        elif command == "shutdown":
            threading.Thread(
                target=handle_shutdown,
                args=(conn, hostname, os_name)
            ).start()
            
        elif command == "restore":
            threading.Thread(
                target=handle_restore,
                args=(conn, hostname)
            ).start()
            
        else:
            conn.sendall(b"Unknown command")
            conn.close()
            
    except Exception as e:
        print(f"Client handling error: {e}")
        try:
            conn.close()
        except:
            pass

def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen()
        print(f"Server started on port: {PORT}")
        print("Awaiting connections...")
        
        while True:
            try:
                conn, addr = s.accept()
                threading.Thread(
                    target=handle_client,
                    args=(conn, addr),
                    daemon=True
                ).start()
            except Exception as e:
                print(f"Server accept error: {e}")

if __name__ == "__main__":
    main()
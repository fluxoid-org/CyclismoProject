sudo apt-get install -y expect
expect -c '
set timeout -1;
spawn android update sdk --no-ui --all --filter tools,platform-tools,build-tools-26.0.1
expect {
    "Do you accept the license" { exp_send "y\r" ; exp_continue }
    eof
}
'

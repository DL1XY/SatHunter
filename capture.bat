@ECHO OFF
%1\ext\rtlsdr\x64\rtl_fm -f %2 -s 11025 -g 29 -p 22 | %1\ext\sox\sox -t raw -e signed -c 1 -b 16 -r 11025 - %1\output\wav\%3.wav
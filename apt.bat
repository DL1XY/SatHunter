@ECHO OFF

SET ATPDEC_PATH= %1\ext\atpdec16win
SET WAV_PATH=%1\output\wav
SET IMAGE_PATH=%1\output\apt

COPY %WAV_PATH%\%2 %ATPDEC_PATH%
CD %ATPDEC_PATH%
atpdec.exe -d %IMAGE_PATH% -i ac %2 
DEL %2
CD %1

RESULT=$(cat $1 | grep -c "QZZCt2ftWILMiOv/bx0NwH1VFPjOT+QCiqkEm96fZOY=")

if [ $RESULT -gt 0 ]; then
  echo "[+] File contains hidden jar"
else
  echo "[-] File does not contain any hidden zxxz-jar files"
fi
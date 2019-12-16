chcp 866
echo off
cls
set /p host="Vvedite ip-adress servera: "
set /p port="Vvedite port servera: "
java -cp jade\lib\jade.jar;out\production\CII2 jade.Boot -container -host %host% -port %port% -agents questions%TIME:~1,-9%%TIME:~3,2%%TIME:~6,2%%TIME:~9,2%:Questions.Questions
pause
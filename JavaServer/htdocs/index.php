<?php
echo "Today is " . date("Y/m/d"); 


echo '<br/><br/><br/>';
for ($i = 1; $i < 10; $i++)
{
    for ($j = 9; $j >= $i; $j--)
    {
        echo $j;
    }
echo '<br/>';
}
?>
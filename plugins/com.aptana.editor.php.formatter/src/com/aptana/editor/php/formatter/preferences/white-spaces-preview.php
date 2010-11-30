<?php
/**
 * White Spaces...
 */
for($i = 0, $j = 0; $i < 8 && $j > 0; $i++, --$j) {
    echo $i . $j;
}
$a = array(1, 2, 3, 4 => 'four');
if($a[1] >= 5) {
    switch ($a[2]) {
        case 3 :
            return !($a[0] > 0);
            break;
        default :
            return $a[0] > 2 ? 10 : 20;
    }
}
$s = $a[2];
$c = new MyClass($s, $a);
$c -> getCount();
$c = ++$s + $a[0]--;
echo "id = ".MyClass::ID;
<?php
/**
 * Preview...
 */
// Functions
function foo() {
do {
} while(true);
try {
print('hello');
} catch(Exception $e) {
} 
}

function bar($a) {
if (true) {
return;
}
// If-Else
if (false) {
echo('hello');
} else
if ($a > 0) {
echo($a);
} else {
echo('oops!');
}
}
// Switch-Case
switch ($a) {
case 1:
print('1');
break;
case 2:
break;
default:
print('none');
}

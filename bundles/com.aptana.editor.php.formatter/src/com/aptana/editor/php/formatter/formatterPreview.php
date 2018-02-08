<?php
/**
 * PHP formatter...
 */
class MyClass {
	public function foo() {
		for($a = 0; $a < 10; $a++) {
			switch ($a) {
				case 1 :
					echo '1';
					break;
				case 5 :
					if($b > 10) {
						echo 'hello';
					} else if($b > 20) {
						echo 'goodbye!';
					} else {
						echo('error!');
					}
					break;
				default : {
					echo $a;
				}
			}
		}
	}
}
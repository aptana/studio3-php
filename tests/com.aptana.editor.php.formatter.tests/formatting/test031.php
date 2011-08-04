==PREFS==
php.formatter.spaces.after.case.colon=1
php.formatter.spaces.before.arrow=1
php.formatter.spaces.before.colon=0
php.formatter.indent.blocks=true
php.formatter.spaces.before.parentheses.closing=0
php.formatter.indent.class.body=true
php.formatter.spaces.before.staticInvocation=0
php.formatter.spaces.after.assignment=1
php.formatter.formatter.on=@formatter:on
php.formatter.spaces.before.declaration.parentheses.opening=0
php.formatter.spaces.before.invocation.parentheses.closing=0
php.formatter.spaces.after.namespaceSeparator=0
php.formatter.spaces.before.array.access.parentheses.closing=0
php.formatter.spaces.after.forSemicolon=1
php.formatter.spaces.after.staticInvocation=0
php.formatter.spaces.before.prefix=0
php.formatter.spaces.after.arithmetic=1
php.formatter.spaces.before.conditional.parentheses.closing=0
php.formatter.line.after.class.declaration=1
php.formatter.newline.before.else=false
php.formatter.line.after.function.declaration=1
php.formatter.spaces.after.conditional.parentheses.opening=0
php.formatter.spaces.after.parentheses=0
php.formatter.spaces.before.conditional=1
php.formatter.spaces.before.loop.parentheses.closing=0
php.formatter.spaces.before.conditional.parentheses.opening=1
php.formatter.formatter.off=@formatter:off
php.formatter.newline.before.dowhile=false
php.formatter.spaces.before.parentheses=0
php.formatter.brace.position.blocks=same.line
php.formatter.wrap.comments.length=80
php.formatter.indent.breakInCase=true
php.formatter.newline.between.array.creation.elements=false
php.formatter.newline.before.if.in.elseif=false
php.formatter.spaces.before.unary=0
php.formatter.formatter.on.off.enabled=false
php.formatter.spaces.after.arrow=1
php.formatter.spaces.after.unary=0
php.formatter.spaces.after.keyValue=1
php.formatter.spaces.before.keyValue=1
php.formatter.indent.switch.body=true
php.formatter.spaces.before.forSemicolon=0
php.formatter.wrap.comments=false
php.formatter.brace.position.class.declaration=same.line
php.formatter.spaces.after.semicolon=1
php.formatter.spaces.after.prefix=0
php.formatter.formatter.tabulation.char=editor
php.formatter.spaces.after.commas=1
php.formatter.indent.case.body=true
php.formatter.spaces.after.postfix=0
php.formatter.spaces.before.relational=1
php.formatter.spaces.before.postfix=0
php.formatter.indent.namespace.blocks=false
php.formatter.brace.position.case.block=same.line
php.formatter.spaces.before.array.access.parentheses.opening=0
php.formatter.spaces.before.namespaceSeparator=0
php.formatter.newline.before.catch=false
php.formatter.spaces.before.commas=0
php.formatter.spaces.after.colon=1
php.formatter.spaces.after.declaration.parentheses.opening=0
php.formatter.spaces.before.semicolon=0
php.formatter.formatter.tabulation.size=4
php.formatter.spaces.before.declaration.parentheses.closing=0
php.formatter.spaces.after.invocation.parentheses.opening=0
php.formatter.line.preserve=1
php.formatter.spaces.after.loop.parentheses.opening=0
php.formatter.formatter.indentation.size=4
php.formatter.indent.function.body=true
php.formatter.brace.position.switch.block=same.line
php.formatter.spaces.before.assignment=1
php.formatter.brace.position.function.declaration=same.line
php.formatter.spaces.before.loop.parentheses.opening=1
php.formatter.indent.php.body=false
php.formatter.spaces.before.case.colon=1
php.formatter.spaces.after.conditional=1
php.formatter.spaces.after.dot=1
php.formatter.spaces.after.relational=1
php.formatter.spaces.before.dot=1
php.formatter.spaces.before.arithmetic=1
php.formatter.spaces.before.invocation.parentheses.opening=0
php.formatter.spaces.after.array.access.parentheses.opening=0
==CONTENT==
<?php
/**
 * @author shalom
 */
header("Cache-Control: no-cache, must-revalidate");
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT\n");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");

date_default_timezone_set("America/Chicago");
$now = new DateTime("now");

$server = "(local)";
$database = "********";
$user = "********";
$pw = "*********";
try {
	$conn = new PDO("sqlsrv:server=$server ; Database=$database", $user, $pw);
	$conn -> setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	//print_r($conn->getAttribute(PDO::ATTR_SERVER_INFO));
} catch(Exception $e) {
	die(print_r($e -> getMessage()));
}

if(isset($_REQUEST['do'])) {
	$do = $_REQUEST['do'];
} else {
	$do = 'undefined';
}
$session = new User($conn);
if($session -> validated !== 1) {
	echo "Unable to validate your login.";
	echo $session -> validated;
	exit();
}
$headhtml = file_get_contents('./templates/headhtml.tpl');
$headhtml = str_replace('{TITLE}', THIS_SCRIPT, $headhtml);
$bodyhtml = '';
$foothtml = '';

//User info at the top, and "breadcrumb"
if($session -> validated == 1) {
	$bodyhtml .= "<span style='float: right'>Hello $session->tname!</span>";
	$bodyhtml .= "<span class='h2'>" . THIS_SCRIPT . "</span><br />";
	$bodyhtml .= "<span style='float: right; font-size: 12px;'>" . $now -> format('M d, Y - g:i a') . "</span>";
	$bodyhtml .= THIS_SCRIPT != 'Menu' ? "<a href='index.php' >Menu</a>" : "";
	if($do !== 'undefined' && (THIS_SCRIPT != 'Menu')) {
		$bodyhtml .= " &raquo; <a href='$_SERVER[URL]'>" . THIS_SCRIPT . "</a>";
	}
	$bodyhtml .= "<hr class='clear' />";

} else {
	$message = '<span class="error">Invalid username or password.</span>';
}

class User {
	var    $uname = '';
	var    $tname = '';
	var    $role = 0;
	var    $validated = 0;
	var    $uid = 0;
	var    $message = '';
	var    $rank = 0;

	function __construct(&$conn) {
		date_default_timezone_set("America/Chicago");

		$loc = strpos($_SERVER['LOGON_USER'], "\\") + 1;
		$iname = substr($_SERVER['LOGON_USER'], $loc);

		if($iname <> '') {
			$query =   "SELECT * FROM MasterStaffList WHERE TTLogin=? and active=1";
			try {
				$stmt = $conn  -> prepare($query);
				$stmt ->  bindParam(1, $iname);
				$stmt ->  execute();
				$user = $stmt ->fetch( PDO :: FETCH_ASSOC ) ;
			} catch ( Exception  $e) {
				echo $e ->  getMessage ( );
			}
		}
		//print_r($user);
		if(isset($user)) {
			$this -> validated = 1;
			$this -> uname = $user['TTLogin'];
			$this -> tname = $user['tname'];
			$this -> role = $user['role'];
			$this-> uid = $user['StaffID'];
			$this ->  rank = $user['Rank'];
		} else {
			if(THIS_SCRIPT    <>  'index') {
				print_r($stmt ->  errorInfo());
			}
		}
	}

}
==FORMATTED==

/**
 * @author shalom
 */
header("Cache-Control: no-cache, must-revalidate");
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT\n");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");

date_default_timezone_set("America/Chicago");
$now = new DateTime("now");

$server = "(local)";
$database = "********";
$user = "********";
$pw = "*********";
try {
	$conn = new PDO("sqlsrv:server=$server ; Database=$database", $user, $pw);
	$conn -> setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	//print_r($conn->getAttribute(PDO::ATTR_SERVER_INFO));
} catch(Exception $e) {
	die(print_r($e -> getMessage()));
}

if (isset($_REQUEST['do'])) {
	$do = $_REQUEST['do'];
} else {
	$do = 'undefined';
}
$session = new User($conn);
if ($session -> validated !== 1) {
	echo "Unable to validate your login.";
	echo $session -> validated;
	exit();
}
$headhtml = file_get_contents('./templates/headhtml.tpl');
$headhtml = str_replace('{TITLE}', THIS_SCRIPT, $headhtml);
$bodyhtml = '';
$foothtml = '';

//User info at the top, and "breadcrumb"
if ($session -> validated == 1) {
	$bodyhtml .= "<span style='float: right'>Hello $session->tname!</span>";
	$bodyhtml .= "<span class='h2'>" . THIS_SCRIPT . "</span><br />";
	$bodyhtml .= "<span style='float: right; font-size: 12px;'>" . $now -> format('M d, Y - g:i a') . "</span>";
	$bodyhtml .= THIS_SCRIPT != 'Menu' ? "<a href='index.php' >Menu</a>" : "";
	if ($do !== 'undefined' && (THIS_SCRIPT != 'Menu')) {
		$bodyhtml .= " &raquo; <a href='$_SERVER[URL]'>" . THIS_SCRIPT . "</a>";
	}
	$bodyhtml .= "<hr class='clear' />";

} else {
	$message = '<span class="error">Invalid username or password.</span>';
}

class User {
	var $uname = '';
	var $tname = '';
	var $role = 0;
	var $validated = 0;
	var $uid = 0;
	var $message = '';
	var $rank = 0;

	function __construct(&$conn) {
		date_default_timezone_set("America/Chicago");

		$loc = strpos($_SERVER['LOGON_USER'], "\\") + 1;
		$iname = substr($_SERVER['LOGON_USER'], $loc);

		if ($iname <> '') {
			$query = "SELECT * FROM MasterStaffList WHERE TTLogin=? and active=1";
			try {
				$stmt = $conn -> prepare($query);
				$stmt -> bindParam(1, $iname);
				$stmt -> execute();
				$user = $stmt -> fetch(PDO::FETCH_ASSOC);
			} catch ( Exception  $e) {
				echo $e -> getMessage();
			}
		}
		//print_r($user);
		if (isset($user)) {
			$this -> validated = 1;
			$this -> uname = $user['TTLogin'];
			$this -> tname = $user['tname'];
			$this -> role = $user['role'];
			$this -> uid = $user['StaffID'];
			$this -> rank = $user['Rank'];
		} else {
			if (THIS_SCRIPT <> 'index') {
				print_r($stmt -> errorInfo());
			}
		}
	}

}

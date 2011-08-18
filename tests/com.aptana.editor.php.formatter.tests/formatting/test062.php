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
php.formatter.brace.position.blocks=new.line
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
php.formatter.brace.position.class.declaration=new.line
php.formatter.spaces.after.semicolon=1
php.formatter.spaces.after.prefix=0
php.formatter.formatter.tabulation.char=editor
php.formatter.spaces.after.commas=1
php.formatter.indent.case.body=true
php.formatter.spaces.after.postfix=0
php.formatter.spaces.before.relational=1
php.formatter.spaces.before.postfix=0
php.formatter.indent.namespace.blocks=false
php.formatter.brace.position.case.block=new.line
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
php.formatter.line.preserve=2
php.formatter.spaces.after.loop.parentheses.opening=0
php.formatter.formatter.indentation.size=4
php.formatter.indent.function.body=true
php.formatter.brace.position.switch.block=new.line
php.formatter.spaces.before.assignment=1
php.formatter.brace.position.function.declaration=new.line
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
class UserTest extends CDbTestCase
{
	public $fixtures = array(
		'users'=>'User',
		'userips'=>'UserIp',
		'usercontacts'=>'UserContact',
		'userprofiles'=>'UserProfile',
	);
	
	
	
	
	
	
	
	public function testCreate ()
	{
		// Create a new user
		$newUser = new User();
		$newUsername = 'newuser';
		$newUser->setAttributes(
			array (
				'username'=>$newUsername,
				'password'=>'newuser',
				'date_joined'=>Yii::app()->localtime->UTCNow,
			)
		);
		$this->assertTrue($newUser->save());
		// log the user's ip
		$newUserIp = new UserIp();
		$newUserIp->setAttributes(
			array(
				'user_id'=>$newUser->id,
				'ip_address'=>gethostbyname(gethostname()),
			)
		);
		$this->assertTrue($newUserIp->save());
		// record the user's contact information
		$newUserContact = new UserContact();
		$newUserContact->setAttributes(
			array(
				'user_id'=>$newUser->id,
				'email'=>'test1@test.com',
			)
		);
		$this->assertTrue($newUserContact->save());
		// set the appropriate profile options
		$newUserProfile = new UserProfile();
		$newUserProfile->setAttributes(
			array(
				'user_id'=>$newUser->id,
				'display_email'=>1,
				'display_im'=>0,
			)
		);
		$this->assertTrue($newUserProfile->save());
		
		// read back the user to assure it was created correctly
		$retrievedUser = User::model()->findByPk($newUser->id);
		$this->assertTrue($retrievedUser instanceof User);
		$this->assertEquals($newUsername,$retrievedUser->username);
		// read ip
		$retrievedUserIp = UserIp::model()->findByPk($newUser->id);
		$this->assertTrue($retrievedUserIp instanceof UserIp);
		$this->assertEquals($newUserIp->ip_address,$retrievedUserIp->ip_address);
		// read contact info
		$retrievedUserContact = UserContact::model()->findByPk($newUser->id);
		$this->assertTrue($retrievedUserContact instanceof UserContact);
		$this->assertEquals($newUserContact->email,$retrievedUserContact->email);
		// read profile info
		$retrievedUserProfile = UserProfile::model()->findByPk($newUser->id);
		$this->assertTrue($retrievedUserProfile instanceof UserProfile);
		$this->assertEquals('124',$retrievedUserProfile->locale_code);
	}
	
	public function testRead ()
	{
		// read a user from the db
		$retrievedUser = $this->users('mod');
		$this->assertTrue($retrievedUser instanceof User);
		$this->assertEquals('mod', $retrievedUser->username);
		// read ip
		$retrievedUserIp = $this->userips('probationary');
		$this->assertTrue($retrievedUserIp instanceof UserIp);
		$this->assertEquals('9.99.999.999', $retrievedUserIp->ip_address);
		// read contact info
		$retrievedUserContact = $this->usercontacts('junior');
		$this->assertTrue($retrievedUserContact instanceof UserContact);
		$this->assertEquals('g@mail.com',$retrievedUserContact->email);
		// read profile info
		$retrievedUserProfile = $this->userprofiles('basic');
		$this->assertTrue($retrievedUserProfile instanceof UserProfile);
		$this->assertEquals('0',$retrievedUserProfile->display_email);
	}
	
	public function testUpdate ()
	{
		for ($i=0; $i < s; $i++) :

		endfor;
		
		foreach ($variable as $key => $value)
		// update a user in the db
		$user = $this->users('basic');
		$updatedPassword = 'different';
		$user->password = $updatedPassword;
		$this->assertTrue($user->save());
		// read back record to be sure update succeeded
		$updatedUser = User::model()->findByPk($user->id);
		$this->assertTrue($updatedUser instanceof User);
		$this->assertEquals('different', $updatedUser->password);
		
		// update a user's profile
		$userProfile = $this->userprofiles('basic');
		$updatedTimezone = '95';
		$userProfile->timezone_code = $updatedTimezone;
		$this->assertTrue($userProfile->save());
		// read back profile to be sure update succeeded
		$updatedProfile = UserProfile::model()->findByPk($user->id);
		$this->assertTrue($updatedProfile instanceof UserProfile);
		$this->assertEquals('95',$updatedProfile->timezone_code);
		// make sure the update was logged
		$sql = "SELECT * FROM tbl_user_info_edits WHERE id = '$user->id'";
		$query = Yii::app()->db->createCommand($sql)->query();
		$this->assertGreaterThanOrEqual('1',$query->rowCount);
	}
	
	public function testDelete ()
	{
		// delete a user (this automatically deletes their ip address entries)
		$user = $this->users('banned');
		$savedUserId = $user->id;
		$this->assertTrue($user->delete());
		$deletedUser = User::model()->findByPk($savedUserId);
		$this->assertEquals(NULL, $deletedUser);
	}
	
}
==FORMATTED==

class UserTest extends CDbTestCase
{
	public $fixtures = array('users' => 'User', 'userips' => 'UserIp', 'usercontacts' => 'UserContact', 'userprofiles' => 'UserProfile', );


	public function testCreate()
	{
		// Create a new user
		$newUser = new User();
		$newUsername = 'newuser';
		$newUser -> setAttributes(array('username' => $newUsername, 'password' => 'newuser', 'date_joined' => Yii::app() -> localtime -> UTCNow, ));
		$this -> assertTrue($newUser -> save());
		// log the user's ip
		$newUserIp = new UserIp();
		$newUserIp -> setAttributes(array('user_id' => $newUser -> id, 'ip_address' => gethostbyname(gethostname()), ));
		$this -> assertTrue($newUserIp -> save());
		// record the user's contact information
		$newUserContact = new UserContact();
		$newUserContact -> setAttributes(array('user_id' => $newUser -> id, 'email' => 'test1@test.com', ));
		$this -> assertTrue($newUserContact -> save());
		// set the appropriate profile options
		$newUserProfile = new UserProfile();
		$newUserProfile -> setAttributes(array('user_id' => $newUser -> id, 'display_email' => 1, 'display_im' => 0, ));
		$this -> assertTrue($newUserProfile -> save());

		// read back the user to assure it was created correctly
		$retrievedUser = User::model() -> findByPk($newUser -> id);
		$this -> assertTrue($retrievedUser instanceof User);
		$this -> assertEquals($newUsername, $retrievedUser -> username);
		// read ip
		$retrievedUserIp = UserIp::model() -> findByPk($newUser -> id);
		$this -> assertTrue($retrievedUserIp instanceof UserIp);
		$this -> assertEquals($newUserIp -> ip_address, $retrievedUserIp -> ip_address);
		// read contact info
		$retrievedUserContact = UserContact::model() -> findByPk($newUser -> id);
		$this -> assertTrue($retrievedUserContact instanceof UserContact);
		$this -> assertEquals($newUserContact -> email, $retrievedUserContact -> email);
		// read profile info
		$retrievedUserProfile = UserProfile::model() -> findByPk($newUser -> id);
		$this -> assertTrue($retrievedUserProfile instanceof UserProfile);
		$this -> assertEquals('124', $retrievedUserProfile -> locale_code);
	}

	public function testRead()
	{
		// read a user from the db
		$retrievedUser = $this -> users('mod');
		$this -> assertTrue($retrievedUser instanceof User);
		$this -> assertEquals('mod', $retrievedUser -> username);
		// read ip
		$retrievedUserIp = $this -> userips('probationary');
		$this -> assertTrue($retrievedUserIp instanceof UserIp);
		$this -> assertEquals('9.99.999.999', $retrievedUserIp -> ip_address);
		// read contact info
		$retrievedUserContact = $this -> usercontacts('junior');
		$this -> assertTrue($retrievedUserContact instanceof UserContact);
		$this -> assertEquals('g@mail.com', $retrievedUserContact -> email);
		// read profile info
		$retrievedUserProfile = $this -> userprofiles('basic');
		$this -> assertTrue($retrievedUserProfile instanceof UserProfile);
		$this -> assertEquals('0', $retrievedUserProfile -> display_email);
	}

	public function testUpdate()
	{
		for ($i = 0; $i < s; $i++)
		:

		endfor;

		foreach ($variable as $key => $value)
		// update a user in the db
			$user = $this -> users('basic');
		$updatedPassword = 'different';
		$user -> password = $updatedPassword;
		$this -> assertTrue($user -> save());
		// read back record to be sure update succeeded
		$updatedUser = User::model() -> findByPk($user -> id);
		$this -> assertTrue($updatedUser instanceof User);
		$this -> assertEquals('different', $updatedUser -> password);

		// update a user's profile
		$userProfile = $this -> userprofiles('basic');
		$updatedTimezone = '95';
		$userProfile -> timezone_code = $updatedTimezone;
		$this -> assertTrue($userProfile -> save());
		// read back profile to be sure update succeeded
		$updatedProfile = UserProfile::model() -> findByPk($user -> id);
		$this -> assertTrue($updatedProfile instanceof UserProfile);
		$this -> assertEquals('95', $updatedProfile -> timezone_code);
		// make sure the update was logged
		$sql = "SELECT * FROM tbl_user_info_edits WHERE id = '$user->id'";
		$query = Yii::app() -> db -> createCommand($sql) -> query();
		$this -> assertGreaterThanOrEqual('1', $query -> rowCount);
	}

	public function testDelete()
	{
		// delete a user (this automatically deletes their ip address entries)
		$user = $this -> users('banned');
		$savedUserId = $user -> id;
		$this -> assertTrue($user -> delete());
		$deletedUser = User::model() -> findByPk($savedUserId);
		$this -> assertEquals(NULL, $deletedUser);
	}

}

#! groovy
node('linux && ant && eclipse && jdk && vncserver') {
	try {
		def studio3Repo = "file://${env.WORKSPACE}/studio3-core/dist/"
		def studio3TestRepo = "file://${env.WORKSPACE}/studio3-core/dist-tests/"
		def phpRepo = "file://${env.WORKSPACE}/dist/"

		buildPlugin {
			dependencies = ['studio3-core': 'Studio/studio3']
			builder = 'com.aptana.php.build'
			properties = ['studio3.p2.repo': studio3Repo]
		}

		testPlugin {
			builder = 'com.aptana.php.tests.build'
			properties = [
				'studio3.p2.repo': studio3Repo,
				'studio3.test.p2.repo': studio3TestRepo,
				'php.p2.repo': phpRepo
			]
			classPattern = 'eclipse/plugins'
			inclusionPattern = 'com.aptana.editor.php*.jar, com.aptana.php.*.jar'
			exclusionPattern = '**/tests/**/*.class,**/*Test*.class,**/Messages.class,com.aptana.*.tests*.jar'
		}

		// If not a PR, trigger downstream builds for same branch
		if (!env.BRANCH_NAME.startsWith('PR-')) {
			build job: "studio3-feature-${env.BRANCH_NAME}", wait: false
		}
	} catch (e) {
		// if any exception occurs, mark the build as failed
		currentBuild.result = 'FAILURE'
		//office365ConnectorSend(message: 'Build failed', status: currentBuild.result, webhookUrl: 'https://outlook.office.com/webhook/ba1960f7-fcca-4b2c-a5f3-095ff9c87b22@300f59df-78e6-436f-9b27-b64973e34f7d/JenkinsCI/5dcba6d96f54460d9264e690b26b663e/72931ee3-e99d-4daf-84d2-1427168af2d9')
		throw e
	} finally {
		step([$class: 'WsCleanup', notFailBuild: true])
	}
} // end node

#! groovy
@Library('pipeline-build') _

timestamps() {
	node('linux && ant && eclipse && jdk && vncserver') {
		try {
			stage('Checkout') {
				checkout scm
			}

			def studio3Repo = "file://${env.WORKSPACE}/studio3-core/dist/"
			def studio3TestRepo = "file://${env.WORKSPACE}/studio3-core/dist-tests/"
			def phpRepo = "file://${env.WORKSPACE}/dist/"

			buildPlugin {
				dependencies = ['studio3-core': '../studio3']
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
				build job: "../studio3-rcp/${env.BRANCH_NAME}", wait: false
			}
		} catch (e) {
			// if any exception occurs, mark the build as failed
			currentBuild.result = 'FAILURE'
			throw e
		} finally {
			step([$class: 'WsCleanup', notFailBuild: true])
		}
	} // end node
} // timestamps

#! groovy
@Library('pipeline-build') _

// Keep logs/reports/etc of last 3 builds, only keep build artifacts of last build
properties([
	buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '1')),
	// specify projects to allow to copy artifacts with a comma-separated list.
  	copyArtifactPermission("/aptana-studio-sync/sync-nightlies-aptana-${env.BRANCH_NAME},../studio3-rcp/${env.BRANCH_NAME}"),
])

timestamps() {
	node('linux && ant && eclipse && jdk && vncserver') {
		try {
			stage('Checkout') {
				checkout scm
			}

			def studio3Repo = "file://${env.WORKSPACE}/studio3-core/dist/"
			def studio3TestRepo = "file://${env.WORKSPACE}/studio3-core/dist-tests/"
			def phpRepo = "file://${env.WORKSPACE}/dist/"
			def eclipseHome = '/usr/local/eclipse-4.7.1a'
			def launcherPlugin = 'org.eclipse.equinox.launcher_1.4.0.v20161219-1356'
			def builderPlugin = 'org.eclipse.pde.build_3.9.300.v20170515-0912'

			buildPlugin {
				dependencies = ['studio3-core': '../studio3']
				builder = 'com.aptana.php.build'
				properties = [
					'studio3.p2.repo': studio3Repo,
					'vanilla.eclipse': eclipseHome,
					'launcher.plugin': launcherPlugin,
					'builder.plugin': builderPlugin,
				]
			}

			testPlugin {
				builder = 'com.aptana.php.tests.build'
				properties = [
					'studio3.p2.repo': studio3Repo,
					'studio3.test.p2.repo': studio3TestRepo,
					'php.p2.repo': phpRepo,
					'vanilla.eclipse': eclipseHome,
					'launcher.plugin': launcherPlugin,
					'builder.plugin': builderPlugin,
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

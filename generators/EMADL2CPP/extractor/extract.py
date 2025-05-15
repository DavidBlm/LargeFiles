import gitlab
import os
import git
import json
import sys

token=str(sys.argv[1])
repositoriesDir=os.path.abspath('repositories/')
outputDir=os.path.abspath('output/')
htmlDir=os.path.join(outputDir, 'html')
jarPath=os.path.abspath('EMADLExtractor.jar')

gl=gitlab.Gitlab('https://git.rwth-aachen.de/', job_token=token)
# gl.auth()

# clean up and create directories
if os.path.exists(repositoriesDir):
	os.system(f'rm -rf {repositoriesDir}')

os.mkdir(repositoriesDir)

if os.path.exists(outputDir):
	os.system(f'rm -rf {outputDir}')

os.mkdir(outputDir)
os.mkdir(htmlDir)

# applications group id
group = gl.groups.get(12996)
projects = group.projects.list(include_subgroups=True, all=True)

for project in projects:
	print(f'Extracting {project.name}')

	repoName=project.name.lower().strip().replace(" ", "_")
	htmlFile=f'{repoName}.html'
	repoDir=os.path.join(repositoriesDir, repoName)

	if not os.path.exists(repoDir):
		git.Git(repositoriesDir).clone(f'https://gitlab-ci-token:{token}@{project.http_url_to_repo.split("https://")[1]}')

	os.system(f'find {repoDir} -name "*.jar" -type f -delete')
	os.system(f'java -jar {jarPath} --html --json --csv -r {repoDir} -o {outputDir} -n {repoName}')

	os.system(f'rm -rf {repoDir}')

	if os.path.exists(os.path.join(outputDir, htmlFile)):
		os.rename(os.path.join(outputDir, htmlFile), os.path.join(htmlDir, htmlFile))
	else:
		print(f'Extracting {project.name} seems to have failed, no html output')


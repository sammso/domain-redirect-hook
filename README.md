Liferay 6.2 Domain Redirect hook

This hook fixes domains so that if your Liferya domain is http://www.mysite.xyz and you type http://mysite.xyz hook does automaticly redirect to http://www.mysite.xyz. It also works other way if the site is http://mysite.xyz and you write http://www.mysite.xyz it will redirect http://mysite.xyz 

only thing you need is this hook

To compile hook

Run 

mvn package 

and deploy resulted war file from target directory to your Liferay 'deploy' folder



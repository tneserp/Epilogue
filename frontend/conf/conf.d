server {
    listen 80;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
        try_files $uri $uri/ /index.html =404;
    }
    
    location /api {
        proxy_pass http://j10c208.p.ssafy.io;
    }
}

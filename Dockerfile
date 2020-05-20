# https://mherman.org/blog/dockerizing-an-angular-app/

# base image
FROM node:14.2

# setup env
ENV PATH /app/node_modules/.bin:$PATH

# switch to working directory
WORKDIR /app

# install and cache dependencies
COPY package.json ./
RUN npm install

# add app
COPY .. ./

# serve
CMD ng serve --host 0.0.0.0


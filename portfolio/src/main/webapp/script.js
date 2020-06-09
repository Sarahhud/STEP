// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ["My birthday is June 4th.",
      "I have a website where I post things I am learning about CS.",
      "I have two sisters.",
      "I have a border collie named Scout."];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

async function loaded(){
    let commentNumber = -1;
    await fetch('/comment-choice').then(response => response.json()).then((number) => {
        commentNumber = parseInt(number);
    });

    fetch('/data').then(response => response.json()).then(async (comments) => {
        const commentListElement = document.getElementById('comment-history');  
        let link = await getLink();    
        if(commentNumber===-1){
            for(let comment of comments){
                commentListElement.appendChild(createCommentElement(comment, document));
            }
        }
        else{
            comments = comments.slice(0,commentNumber);
            for(let comment of comments){
                commentListElement.appendChild(createCommentElement(comment, document));
            }
        }
        if(link !== "null"){
            const logoutElement = document.createElement('div');
            logoutElement.innerHTML = `Click <a href=${link}>here</a> to logout!`;
            commentListElement.appendChild(logoutElement);
        }
    });
}

function getLink(){
    return fetch('/log-out').then(response => response.json()).then((url) => {
        return String(url);
    });
}

function createCommentElement(comment, document) {
  const commentElement = document.createElement('p');
  commentElement.className = 'comment';

  const authorElement = document.createElement('div');
  authorElement.innerText = comment.email;

  const titleElement = document.createElement('div');
  titleElement.innerText = comment.title;

  const textElement = document.createElement('div');
  textElement.className = 'text';
  textElement.innerText = comment.text;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);

    // Remove the comment from the DOM.
    commentElement.remove();
  });
  textElement.append(deleteButtonElement);
  commentElement.appendChild(authorElement);
  commentElement.appendChild(titleElement);
  commentElement.appendChild(textElement);
  return commentElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}
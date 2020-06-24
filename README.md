# Telerest
The simplest implementation of the Rest API to send messages to telegram.
With this lightweight app you can create your own bot, which sends messages
by HTTP requests from your CI/CD systems, smart home devices, monitoring system, etc.

## How to register new bot
There is an official instruction how to register your own bot: https://core.telegram.org/bots#6-botfather

After choosing your bot **name** (how you see it in a chat), **username** (how you find it to add to a chat)
you get **HTTP API token** (the only one mandatory property you need to pass to container,
example: `1081244809:AAE5U9v-f4E_1m5ZrFz4tMynbQOZbVN5Szk`).

## How to run
```bash
docker run -e "TELEGRAM_TOKEN=<YOUR-TOKEN-HERE>" -p 8080:8080 artemkaxboy/telerest:0.0.2
```
That's all. You pass your token and allow the container to use tcp port on your host machine.

## How to use
### Start conversation
You have to start conversation with the bot or add it to a group, or it won't be able to send you any message.

### Getting chat ID
Send it `/start` or `/chatid` command, and you give current chat ID. This is the mandatory property to send a message
unless you set `TELEGRAM_DEFAULT_CHAT_ID` variable.

### Send message
To send a message you have to perform POST request:
```bash
curl --location --request POST 'localhost:8080/api/v1/message' \
    --header 'Content-Type: application/json' \
    --data-raw '{ "chatId": "-421511166", "text": "Hello" }'
```
* If success, you will give code 200 response and technical information about sent message.
* If the service available but something went wrong you will give code 200 response containing detailed error message.

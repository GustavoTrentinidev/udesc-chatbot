# Twilio WhatsApp Sandbox Setup

## 1. Create a Twilio account

Go to [twilio.com](https://www.twilio.com) and sign up for a free account.
No credit card required for the sandbox.

## 2. Get your credentials

In the [Twilio Console](https://console.twilio.com), on the home page you will find:
- **Account SID**
- **Auth Token**

Copy both into your `.env` file:

```env
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_auth_token_here
TWILIO_FROM_NUMBER=whatsapp:+14155238886
```

> The `TWILIO_FROM_NUMBER` is always `whatsapp:+14155238886` — this is Twilio's shared sandbox number, the same for everyone.

## 3. Activate the WhatsApp Sandbox

1. In the Twilio Console, go to **Messaging > Try it out > Send a WhatsApp message**
2. You will see a sandbox number and a join code, e.g. `join <word>-<word>`
3. From your Brazilian WhatsApp, send that join code to `+1 415 523 8886`
4. You will receive a confirmation message — your number is now opted in

> You need to repeat this once per number you want to test with. The opt-in lasts until you restart the sandbox.

## 4. Configure the webhook

1. In the Twilio Console, go to **Messaging > Try it out > Send a WhatsApp message**
2. Scroll down to **Sandbox settings**
3. In the field **"When a message comes in"**, enter your ngrok URL:
   ```
   https://<your-ngrok-url>/webhook/twilio
   ```
4. Set the method to **HTTP POST**
5. Click **Save**

## 5. Start the application

Make sure your `.env` has the Twilio credentials, then:

```bash
docker compose up --build
```

## 6. Test

Send any message from your WhatsApp to `+1 415 523 8886`.

The bot will respond with the menu. To navigate, reply with the **number** of the option you want:

```
1. Campus information
2. Registration
...
```

Reply with `1`, `2`, etc. to select options.

## Troubleshooting

- **No response**: Check `docker compose logs backend` for errors
- **"Not joined"**: You need to send the join code to the sandbox number first (step 3)
- **ngrok URL changed**: Every time ngrok restarts it gets a new URL — update the webhook in Twilio Console

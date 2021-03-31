sed -i s/CATOGRAM_ALIAS/${KEY_ALIAS}/ gradle.properties
sed -i s/CATOGRAM_PASSWORD/${KEY_PASS}/ gradle.properties
sed -i s/CATOGRAM_STORE_PASSWORD/${STORE_PASS}/ gradle.properties
sed -i s/CATOGRAM_APP_ID/${API_KEY}/ TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java
sed -i s/CATOGRAM_API_HASH/${API_HASH}/ TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java
#-------------------#
./gradlew assembleAfatRelease | > /dev/null
#-------------------#
cd TMessagesProj/build/outputs/apk/afat/release
curl -F chat_id="-1001293922100" -F document=@app.apk -F caption="Head: $(git log --pretty=format:'%h' -n 1)" -F parse_mode=markdown https://bot.pokurt.me/bot${TG_BOT_KEY}/sendDocument
#-------------------# 

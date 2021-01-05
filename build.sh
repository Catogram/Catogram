curl -s -X POST "https://api.telegram.org/bot${TG_BOT_KEY}/sendMessage" -d chat_id="-1001293922100" \
  -d "disable_web_page_preview=true" \
  -d "parse_mode=markdown" \
  -d text="Build triggered"
#-------------------#
sed -i s/CATOGRAM_ALIAS/${KEY_ALIAS}/
sed -i s/CATOGRAM_PASSWORD/${KEY_PASS}/
sed -i s/CATOGRAM_STORE_PASSWORD/${STORE_PASS}/
#-------------------#
./gradlew assembleAfatRelease
#-------------------#
cd TMessagesProj/build/outputs/apk/afat/release
curl -s -X POST "https://api.telegram.org/bot${TG_BOT_KEY}/sendMessage" -d chat_id="-1001293922100" \
  -d "disable_web_page_preview=true" \
  -d "parse_mode=markdown" \
  -d text="$(curl -F "file=@app.apk" https://x0.at/ | cat)"
#-------------------# 

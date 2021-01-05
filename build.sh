curl -s -X POST "https://api.telegram.org/bot${TG_BOT_KEY}/sendMessage" -d chat_id="-1001293922100" \
  -d "disable_web_page_preview=true" \
  -d "parse_mode=markdown" \
  -d text="Build triggered"
#-------------------#
sed -i s/CATOGRAM_ALIAS/${KEY_ALIAS}/
sed -i s/CATOGRAM_PASSWORD/${KEY_PASS}/
sed -i s/CATOGRAM_STORE_PASSWORD/${STORE_PASS}/
#-------------------#
./gradlew assembleRelease
#-------------------#
cd TMessagesProj/build/outputs/apk/*/release
curl --progress-bar -F document=@"app.apk" "https://api.telegram.org/bot${TG_BOT_KEY}/sendDocument" \
-F chat_id="-1001293922100"  \
-F "disable_web_page_preview=true" \
-F "parse_mode=html" \
-F caption="Build done"
#-------------------# 

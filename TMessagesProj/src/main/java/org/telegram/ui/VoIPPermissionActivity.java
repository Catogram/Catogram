package org.telegram.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.telegram.messenger.voip.VoIPService;
import org.telegram.ui.Components.voip.VoIPHelper;

public class VoIPPermissionActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 101);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == 101) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (VoIPService.getSharedInstance() != null)
					VoIPService.getSharedInstance().acceptIncomingCall();
				finish();
				startActivity(new Intent(this, VoIPActivity.class));
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
						if (VoIPService.getSharedInstance() != null)
							VoIPService.getSharedInstance().declineIncomingCall();
						VoIPHelper.permissionDenied(this, new Runnable() {
							@Override
							public void run() {
								finish();
							}
						});
						return;
					} else {
						finish();
					}
				}
			}
		}
	}
}

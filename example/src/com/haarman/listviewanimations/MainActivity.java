/*
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haarman.listviewanimations;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.haarman.listviewanimations.appearanceexamples.AppearanceExamplesActivity;
import com.haarman.listviewanimations.itemmanipulationexamples.ItemManipulationsExamplesActivity;

public class MainActivity extends Activity {

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		menu.findItem(R.id.menu_main_donate).setVisible(mService != null);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_main_github:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://nhaarman.github.io/ListViewAnimations?ref=app"));
			startActivity(intent);
			return true;
		case R.id.menu_main_beer:
			buy("beer");
			return true;
		case R.id.menu_main_beer2:
			buy("beer2");
			return true;
		case R.id.menu_main_beer3:
			buy("beer3");
			return true;
		case R.id.menu_main_beer4:
			buy("beer4");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onGoogleCardsExampleClicked(View view) {
		Intent intent = new Intent(this, GoogleCardsActivity.class);
		startActivity(intent);
	}

	public void onGridViewExampleClicked(View view) {
		Intent intent = new Intent(this, GridViewActivity.class);
		startActivity(intent);
	}

	public void onAppearanceClicked(View view) {
		Intent intent = new Intent(this, AppearanceExamplesActivity.class);
		startActivity(intent);
	}

	public void onItemManipulationClicked(View view) {
		Intent intent = new Intent(this, ItemManipulationsExamplesActivity.class);
		startActivity(intent);
	}

	private IInAppBillingService mService;

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			//			supportInvalidateOptionsMenu();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
			//			supportInvalidateOptionsMenu();

			new Thread() {

				@Override
				public void run() {
					try {
						Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

						int response = ownedItems.getInt("RESPONSE_CODE");
						if (response == 0) {
							ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

							if (purchaseDataList != null) {
								for (int i = 0; i < purchaseDataList.size(); ++i) {
									String purchaseData = purchaseDataList.get(i);
									JSONObject json = new JSONObject(purchaseData);
									mService.consumePurchase(3, getPackageName(), json.getString("purchaseToken"));
								}
							}
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mServiceConn != null) {
			unbindService(mServiceConn);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();

			new Thread() {

				@Override
				public void run() {
					try {
						JSONObject json = new JSONObject(data.getStringExtra("INAPP_PURCHASE_DATA"));
						mService.consumePurchase(3, getPackageName(), json.getString("purchaseToken"));
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

			}.start();
		}
	}

	private void buy(String sku) {
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), sku, "inapp", "bGoa+V7g/ysDXvKwqq+JTFn4uQZbPiQJo4pf9RzJ");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			if (pendingIntent != null) {
				startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (SendIntentException e) {
			e.printStackTrace();
		}
	}
}

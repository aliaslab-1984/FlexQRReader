# Install guide:

Welcome to FlexQRReader, an SDK that lets you scan QR Codes without worrying about compatibility issues.

To install the SDK, first add this activities into your app manifest:

```xml
        <!-- GooglePlay version -->
        <activity
            android:name="net.aliaslab.securecall.flexqrreader.playvision.QrCodeScanActivity"
            android:parentActivityName=".activities.DefaultQRActivity">AliasAuth</activity>

        <!-- Default QRActivity launcher -->
        <activity
            android:name="net.aliaslab.securecall.flexqrreader.DefaultQRActivity"
            android:parentActivityName=".MainActivity">AliasAuth</activity>

        <!-- Zxing Zebra -->
        <activity
            android:name="net.aliaslab.securecall.flexqrreader.zxing.android.CaptureActivity"
            android:clearTaskOnLaunch="true"
            android:screenOrientation="sensorPortrait"
            android:stateNotNeeded="true"
            android:theme="@style/CaptureTheme"
            android:windowSoftInputMode="stateAlwaysHidden">
            <intent-filter>
                <action android:name="net.aliaslab.scotp.modern.demo.SCAN" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
```

You have to replace *.MainActivity* with the activity that should show the DefaultQRActivity.

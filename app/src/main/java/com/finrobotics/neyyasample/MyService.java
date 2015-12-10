package com.finrobotics.neyyasample;

import com.finrobotics.neyyasdk.core.NeyyaBaseService;

/**
 * Third party service. It should extend NeyyaBaseService.
 * Start this service leads to execution of base service.
 * Don't forget to start the service in different process because NeyyaBaseService includes lot of threads
 * and these threads sleep simultaneously to attain a smooth bluetooth flow. If it is not in other process UI
 * will also hang because service is running on UI thread.
 *
 * Please see the AndroidManifest.xml file to see how service is put in different process.
 *
 * Created by zac on 23/09/15.
 */
public class MyService extends NeyyaBaseService{

}

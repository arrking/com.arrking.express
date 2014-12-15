'use strict';
angular.module('tory.services', ['config'])

.service('store', function($log) {

	this.saveJPushRegistrationID = function(id) {
		window.localStorage.setItem('TORY_JPUSH_REGID', id);
	}
})

.service('jpush', function($q, $log, cfg, store) {

	function _getRegistradionID() {
		window.plugins.jPushPlugin.getRegistrationID(function(data) {
			try {
				$log.debug(">>TORY JPushPlugin:registrationID is " + data);
				store.saveJPushRegistrationID(data);
			} catch (exception) {
				$log.debug(exception);
			}
		});
	}

	function _setTag() {
		$log.debug(">>TORY get jpush tags " + JSON.stringify(cfg.jpush.tags));
		window.plugins.jPushPlugin.setTags(cfg.jpush.tags);
	}

	this.start = function() {
		if (window.plugins.jPushPlugin) {
			$log.debug(">>TORY get jPushPlugin ...");
			// get RegistrationID
			_getRegistradionID();
			// set tags
			_setTag();
			// callback for openning pushed message ?
			// need to figure out what this receiveMessageInAndroidCallback about ?
			// receiveMessageInAndroidCallback v.s. openNotificationInAndroidCallback
			// window.plugins.jPushPlugin.receiveMessageInAndroidCallback(function(data) {
			// 	$log.debug("JPushPlugin receiveMessageInAndroidCallback");
			// 	$log.debug(data);
			// });
		} else {
			$log.error(">>TORY can not get jPushPlugin ...");
		}
	}

	window.JpushOpenNotificationInAndroidCallback = function(data){
		$log.debug(">>TORY JpushOpenNotificationInAndroidCallback " + data);
		$log.debug(">>TORY JpushOpenNotificationInAndroidCallback " + typeof data);
	}

})

;
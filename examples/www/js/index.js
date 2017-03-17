/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');

        // //IOS:58a544ba Android:58a3c94e
        // Xfyun.init("58a544ba", "auto", function(){
        // 	alert("ok");
        // });

        // var cloudGrammar = "#ABNF 1.0 UTF-8;\n"+
        //                 "language zh-CN;\n"+
        //                 "mode voice;\n"+
        //                 "root $main;\n"+
        //                 "$main = $place1 到 $place2;\n"+
        //                 "$place1 = 上海|合肥;\n"+
        //                 "$place2 = 北京|武汉|南京|天津|东京;";
        // Xfyun.buildGrammar(cloudGrammar, 
        // 	function(grammarId){
        // 		alert("grammarId=" + grammarId);
        // 		Xfyun.startListeningGrammar(grammarId, function(data){
        // 			console.log("=========== " + JSON.stringify(data));

        // 			// Xfyun.cancelGrammar(function(){
        // 			// 	console.log("停止成功");
        // 			// 	Xfyun.startListeningGrammar(grammarId, function(ddd){
        // 			// 		console.log(ddd.action);
        // 			// 	}, function(xxx){
        // 			// 		console.log("在此监听出错"+xxx);
        // 			// 	});
        // 			// }, function(){console.log("停止失败")});

        // 		}, function(err){
        // 			console.log("=========== " + err);
        // 		});
        // 	}, 
        // 	function(err){
        //     	alert(err);
        //     }
        // );

        FingerprintAuth.isAvailable(function() {
            alert("[success]设备支持指纹验证");
        }, function() {
            alert("[error]设备不支持指纹验证");
        });


    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();
/**
 *  Driver Hubitat -  Amplificador LOUD 4 AP 100
 *
 *  Copyright 2025 VH 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *            --- Driver para AMP Loud 4 AP 100
 *           v.1  26/03/2025 - BETA 1. 
*/
metadata {
    definition (
        name: "Loud Audio 4 AP 100 Amplifier (HTTP)",
        namespace: "VH",
        author: "VH"
        
    ) {
        capability "AudioVolume"
        capability "Switch"
        capability "Refresh"
        
        attribute "zone1Input", "string"
        attribute "zone2Input", "string"
        attribute "zone1Volume", "number"
        attribute "zone2Volume", "number"
        attribute "zone1Mute", "string"
        attribute "zone2Mute", "string"
        attribute "bass", "number"
        attribute "treble", "number"
        attribute "mode", "string"
        attribute "connection", "string"
        
        command "setZone1Input", [[name:"Input*", type: "ENUM", 
            constraints: ["AUX1", "AUX2", "OPTICO", "BLUETOOTH", "WIFI"]]]
        command "setZone2Input", [[name:"Input*", type: "ENUM", 
            constraints: ["AUX1", "AUX2", "OPTICO", "BLUETOOTH", "WIFI"]]]
        command "setZone1Volume", [[name:"Volume*", type: "NUMBER"]]
        command "setZone2Volume", [[name:"Volume*", type: "NUMBER"]]
        command "zone1VolumeUp"
        command "zone1VolumeDown"
        command "zone2VolumeUp"
        command "zone2VolumeDown"
        command "zone1MuteOn"
        command "zone1MuteOff"
        command "zone2MuteOn"
        command "zone2MuteOff"
        command "setBass", [[name:"Bass*", type: "NUMBER"]]
        command "setTreble", [[name:"Treble*", type: "NUMBER"]]
        command "bassUp"
        command "bassDown"
        command "trebleUp"
        command "trebleDown"
        command "setMode", [[name:"Mode*", type: "ENUM", 
            constraints: ["DUAL", "SINGLE", "SURROUND"]]]
    }
    
    preferences {
        input name: "ipAddress", type: "text", title: "IP Address", required: true
        input name: "pollingInterval", type: "number", title: "Polling Interval (minutes)", defaultValue: 5, range: "1..60"
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def installed() {
    log.info "Loud Audio 4 AP 100 (HTTP) driver installed"
    initialize()
}

def updated() {
    log.info "Loud Audio 4 AP 100 (HTTP) driver updated"
    initialize()
}

def initialize() {
    unschedule()
    
    // Set initial connection status
    sendEvent(name: "connection", value: "disconnected")
    
    // Start polling
    if (pollingInterval) {
        runEvery(pollingInterval * 60, "pollDevice")
    }
    
    if (logEnable) runIn(1800, logsOff)
    
    // Initial refresh
    pollDevice()
}

def logsOff() {
    log.warn "Debug logging disabled"
    device.updateSetting("logEnable", [value:"false", type:"bool"])
}

// Command methods
def on() {
    sendHttpCommand("PWON")
    sendEvent(name: "switch", value: "on")
    pollDevice()
}

def off() {
    sendHttpCommand("PWOFF")
    sendEvent(name: "switch", value: "off")
    pollDevice()
}

def setVolume(volume) {
    setZone1Volume(volume)
}

def volumeUp() {
    zone1VolumeUp()
}

def volumeDown() {
    zone1VolumeDown()
}

def mute() {
    zone1MuteOn()
}

def unmute() {
    zone1MuteOff()
}

def setZone1Input(input) {
    sendHttpCommand("SI${input}")
    sendEvent(name: "zone1Input", value: input)
    pollDevice()
}

def setZone2Input(input) {
    sendHttpCommand("SI${input}")
    sendEvent(name: "zone2Input", value: input)
    pollDevice()
}

def setZone1Volume(volume) {
    if (volume < 0) volume = 0
    if (volume > 100) volume = 100
    sendHttpCommand("VL${volume}")
    sendEvent(name: "zone1Volume", value: volume)
    sendEvent(name: "volume", value: volume)
    pollDevice()
}

def setZone2Volume(volume) {
    if (volume < 0) volume = 0
    if (volume > 100) volume = 100
    sendHttpCommand("VL${volume}")
    sendEvent(name: "zone2Volume", value: volume)
    pollDevice()
}

def zone1VolumeUp() {
    sendHttpCommand("VLUP")
    def current = device.currentValue("zone1Volume") ?: 0
    def newVol = Math.min(current + 1, 100)
    sendEvent(name: "zone1Volume", value: newVol)
    sendEvent(name: "volume", value: newVol)
    pollDevice()
}

def zone1VolumeDown() {
    sendHttpCommand("VLDOWN")
    def current = device.currentValue("zone1Volume") ?: 0
    def newVol = Math.max(current - 1, 0)
    sendEvent(name: "zone1Volume", value: newVol)
    sendEvent(name: "volume", value: newVol)
    pollDevice()
}

def zone2VolumeUp() {
    sendHttpCommand("VLUP")
    def current = device.currentValue("zone2Volume") ?: 0
    def newVol = Math.min(current + 1, 100)
    sendEvent(name: "zone2Volume", value: newVol)
    pollDevice()
}

def zone2VolumeDown() {
    sendHttpCommand("VLDOWN")
    def current = device.currentValue("zone2Volume") ?: 0
    def newVol = Math.max(current - 1, 0)
    sendEvent(name: "zone2Volume", value: newVol)
    pollDevice()
}

def zone1MuteOn() {
    sendHttpCommand("MUON")
    sendEvent(name: "zone1Mute", value: "on")
    sendEvent(name: "mute", value: "muted")
    pollDevice()
}

def zone1MuteOff() {
    sendHttpCommand("MUOFF")
    sendEvent(name: "zone1Mute", value: "off")
    sendEvent(name: "mute", value: "unmuted")
    pollDevice()
}

def zone2MuteOn() {
    sendHttpCommand("MUON")
    sendEvent(name: "zone2Mute", value: "on")
    pollDevice()
}

def zone2MuteOff() {
    sendHttpCommand("MUOFF")
    sendEvent(name: "zone2Mute", value: "off")
    pollDevice()
}

def setBass(level) {
    if (level < -15) level = -15
    if (level > 15) level = 15
    sendHttpCommand("BS${level}")
    sendEvent(name: "bass", value: level)
    pollDevice()
}

def setTreble(level) {
    if (level < -15) level = -15
    if (level > 15) level = 15
    sendHttpCommand("TB${level}")
    sendEvent(name: "treble", value: level)
    pollDevice()
}

def bassUp() {
    sendHttpCommand("BSUP")
    def current = device.currentValue("bass") ?: 0
    def newLevel = Math.min(current + 1, 15)
    sendEvent(name: "bass", value: newLevel)
    pollDevice()
}

def bassDown() {
    sendHttpCommand("BSDOWN")
    def current = device.currentValue("bass") ?: 0
    def newLevel = Math.max(current - 1, -15)
    sendEvent(name: "bass", value: newLevel)
    pollDevice()
}

def trebleUp() {
    sendHttpCommand("TBUP")
    def current = device.currentValue("treble") ?: 0
    def newLevel = Math.min(current + 1, 15)
    sendEvent(name: "treble", value: newLevel)
    pollDevice()
}

def trebleDown() {
    sendHttpCommand("TBDOWN")
    def current = device.currentValue("treble") ?: 0
    def newLevel = Math.max(current - 1, -15)
    sendEvent(name: "treble", value: newLevel)
    pollDevice()
}

def setMode(mode) {
    def command = ""
    switch(mode.toUpperCase()) {
        case "DUAL":
            command = "SO1"
            break
        case "SINGLE":
            command = "SO2"
            break
        case "SURROUND":
            command = "SO3"
            break
        default:
            log.warn "Invalid mode: ${mode}"
            return
    }
    sendHttpCommand(command)
    sendEvent(name: "mode", value: mode.toLowerCase())
    pollDevice()
}

def refresh() {
    pollDevice()
}

def pollDevice() {
    if (logEnable) log.debug "Polling device status"
    
    def params = [
        uri: "http://${ipAddress}/v2/send?LOUDCM",
        timeout: 5
    ]
    
    try {
        httpGet(params) { resp ->
            if (resp.status == 200) {
                sendEvent(name: "connection", value: "connected")
                def data = resp.data
                if (logEnable) log.debug "Poll response: ${data}"
                
                if (data.status) {
                    if (data.data instanceof List) {
                        // Multi-zone response
                        def zone1 = data.data[0]
                        def zone2 = data.data[1]
                        
                        sendEvent(name: "switch", value: zone1.power ? "on" : "off")
                        sendEvent(name: "zone1Volume", value: zone1.volume)
                        sendEvent(name: "zone2Volume", value: zone2.volume)
                        sendEvent(name: "volume", value: zone1.volume)
                        sendEvent(name: "zone1Mute", value: zone1.mute ? "on" : "off")
                        sendEvent(name: "zone2Mute", value: zone2.mute ? "on" : "off")
                        sendEvent(name: "mute", value: zone1.mute ? "muted" : "unmuted")
                        sendEvent(name: "bass", value: zone1.bass)
                        sendEvent(name: "treble", value: zone1.treble)
                        
                        // Map input number to input name
                        def inputNames = ["AUX1", "AUX2", "OPTICO", "BLUETOOTH", "WIFI"]
                        def zone1Input = zone1.input < inputNames.size() ? inputNames[zone1.input] : "UNKNOWN"
                        def zone2Input = zone2.input < inputNames.size() ? inputNames[zone2.input] : "UNKNOWN"
                        
                        sendEvent(name: "zone1Input", value: zone1Input)
                        sendEvent(name: "zone2Input", value: zone2Input)
                    } else {
                        // Single zone response
                        def zone = data.data
                        sendEvent(name: "switch", value: zone.power ? "on" : "off")
                        sendEvent(name: "zone1Volume", value: zone.volume)
                        sendEvent(name: "volume", value: zone.volume)
                        sendEvent(name: "zone1Mute", value: zone.mute ? "on" : "off")
                        sendEvent(name: "mute", value: zone.mute ? "muted" : "unmuted")
                        sendEvent(name: "bass", value: zone.bass)
                        sendEvent(name: "treble", value: zone.treble)
                        
                        def inputNames = ["AUX1", "AUX2", "OPTICO", "BLUETOOTH", "WIFI"]
                        def zoneInput = zone.input < inputNames.size() ? inputNames[zone.input] : "UNKNOWN"
                        sendEvent(name: "zone1Input", value: zoneInput)
                    }
                }
            } else {
                log.warn "Poll failed with status ${resp.status}"
                sendEvent(name: "connection", value: "disconnected")
            }
        }
    } catch (Exception e) {
        log.warn "Poll failed: ${e.message}"
        sendEvent(name: "connection", value: "disconnected")
    }
}

private sendHttpCommand(command) {
    if (!ipAddress) {
        log.warn "IP Address not configured"
        return
    }
    
    def url = "http://${ipAddress}/v2/send?LOUDCM=${command}"
    if (logEnable) log.debug "Sending HTTP command: ${url}"
    
    try {
        httpGet([uri: url, timeout: 3]) { resp ->
            if (logEnable) log.debug "Command response: ${resp.status}"
        }
    } catch (Exception e) {
        log.warn "Failed to send HTTP command: ${e.message}"
        sendEvent(name: "connection", value: "disconnected")
    }
}

def uninstalled() {
    unschedule()
}

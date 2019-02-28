/*
 * Copyright (C) 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.appunite.websocket.rx.`object`

import com.appunite.websocket.rx.RxWebSockets
import com.appunite.websocket.rx.messages.RxEventBinaryMessage
import com.appunite.websocket.rx.`object`.messages.RxObjectEvent
import com.appunite.websocket.rx.`object`.messages.RxObjectEventMessage
import com.appunite.websocket.rx.messages.RxEvent
import com.appunite.websocket.rx.messages.RxEventConnected
import com.appunite.websocket.rx.messages.RxEventDisconnected
import com.appunite.websocket.rx.messages.RxEventStringMessage
import com.appunite.websocket.rx.`object`.messages.RxObjectEventWrongBinaryMessageFormat
import com.appunite.websocket.rx.`object`.messages.RxObjectEventConnected
import com.appunite.websocket.rx.`object`.messages.RxObjectEventWrongStringMessageFormat
import com.appunite.websocket.rx.`object`.messages.RxObjectEventDisconnected

import io.reactivex.functions.Function
import okhttp3.WebSocket

import okio.ByteString
import io.reactivex.Observable

/**
 * This class allows to retrieve json messages from websocket
 */
class RxObjectWebSockets
/**
 * Creates [RxObjectWebSockets]
 *
 * @param rxWebSockets     socket that is used to connect to server
 * @param objectSerializer that is used to parse messages
 */
(private val rxWebSockets: RxWebSockets, private val objectSerializer: ObjectSerializer) {

    /**
     * Returns observable that connected to a websocket and returns [RxObjectEvent]s
     *
     * @return Observable that connects to websocket
     * @see RxWebSockets.webSocketObservable
     */
    fun webSocketObservable(): Observable<RxObjectEvent> {
        return rxWebSockets.webSocketObservable()
                .map(object : Function<RxEvent, RxObjectEvent> {
                    @Throws(Exception::class)
                    override fun apply(rxEvent: RxEvent): RxObjectEvent {
                        return when (rxEvent) {
                            is RxEventConnected -> RxObjectEventConnected(jsonSocketSender(rxEvent.sender()))
                            is RxEventDisconnected -> RxObjectEventDisconnected(rxEvent.exception())
                            is RxEventStringMessage -> parseMessage(rxEvent)
                            is RxEventBinaryMessage -> parseMessage(rxEvent)
                            else -> throw RuntimeException("Unknown message type")
                        }
                    }


                    private fun parseMessage(stringMessage: RxEventStringMessage): RxObjectEvent {
                        val message = stringMessage.message()
                        val `object`: Any
                        try {
                            `object` = objectSerializer.serialize(message)
                        } catch (e: ObjectParseException) {
                            return RxObjectEventWrongStringMessageFormat(jsonSocketSender(stringMessage.sender()), message, e)
                        }

                        return RxObjectEventMessage(jsonSocketSender(stringMessage.sender()), `object`)
                    }

                    private fun parseMessage(binaryMessage: RxEventBinaryMessage): RxObjectEvent {
                        val message = binaryMessage.message()
                        val `object`: Any
                        try {
                            `object` = objectSerializer.serialize(message)
                        } catch (e: ObjectParseException) {
                            return RxObjectEventWrongBinaryMessageFormat(jsonSocketSender(binaryMessage.sender()), message, e)
                        }

                        return RxObjectEventMessage(jsonSocketSender(binaryMessage.sender()), `object`)
                    }

                })
    }

    private fun jsonSocketSender(sender: WebSocket): ObjectWebSocketSender {
        return ObjectWebSocketSender { message ->
            if (objectSerializer.isBinary(message)) {
                sender.send(ByteString.of(*objectSerializer.deserializeBinary(message)))
            } else {
                sender.send(objectSerializer.deserializeString(message))
            }
        }
    }
}

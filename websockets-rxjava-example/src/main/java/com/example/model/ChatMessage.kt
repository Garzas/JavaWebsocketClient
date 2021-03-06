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

package com.example.model

class ChatMessage(val message: String, val from: String) : Message(MessageType.CHAT) {

    fun message(): String = message

    fun from(): String {
        return from
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ChatMessage) return false

        val that = o as ChatMessage?

        return message == that!!.message && from == that.from

    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + from.hashCode()
        return result
    }

    override fun toString(): String {
        return "RegisterRequest{" +
                "message='" + message + '\''.toString() +
                "from='" + from + '\''.toString() +
                "} " + super.toString()
    }
}

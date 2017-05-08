<p align="center" >
  <img src="https://avatars0.githubusercontent.com/u/21214996?v=3" alt="Connect" title="Connect">
</p>

## Introduction

[Connect](https://www.connect.im/) is an open source point-to-point encryption instant messaging APP.
You can use [Connect](https://www.connect.im/) to send texts, voices, pictures, videos and even Bitcoin.
In both one-on-one chat and group chat, all the messages including text, picture, voices, video, etc. are sent via the shared key encryption negotiated by both sides of the chat.
Any third party other than the both sides including the server can't decrypt the messages.
Different from other instant messaging tools provided by other Internet giants, [Connect](https://www.connect.im/) offers a higher level of security and privacy. protection.
It protects your chatting contents from being eavesdropped by any third party such as employers and government.
It protects your personal data such as telephone numbers and friends from being utilized by any third party such as marketing personnel and advertisers.
Considering "PRISM" scandal as well as multiple network fraud cases caused by information leakage, each of us should defending "freedom of speech" and "personal privacy”.
So [Connect](https://www.connect.im/) is suitable for everyone.

## Encryption
[Connect](https://www.connect.im/) uses advanced and open-source symmetric encryption algorithms to secure both parties' information and the communication between the client and the server, and anyone can authenticate.
The end-to-end encrypted communication between both sides of the session as well as the encrypted communication between client-side and server uses the key negotiation method to make double layer encryption, with the steps as below:
1. Session initiator_A use the agreed ECC (elliptic curve cryptography) locally to generate a pair of key(Public_key_A,Private_key_A) and a 512-bit random number salt, and send the random number “saltA” and “Public_key_A” to receiver_B after AES256-GCM encrypted.
2. The receiver_B receives “PublicKey_A” and “saltA” after decrypt. then use the same ECC (elliptic curve cryptography) generate a pair of key(Public_Key_B,Private_Key_b) and a 512-bit random number salt, and send the random number “saltB” and “Public_key_B” to initiator_A after AES256-GCM encrypted.
3. Initiator_A get “saltB” and “Public_key_B”. Then both initiator_A and receiver_B get ECDH_Key
ECDH_Key = ECDH(PrivateKey_A, PublicKey_B) = ECDH(PrivateKey_B, PublicKey_A)
4. Then the PBKDF2key expansion algorithm to obtain the negotiated key "K" by ECDH_KEY and random number salts of both sides.
Shared_key = PBKDF2(HMAC-SHA512, ecdhKey, saltA^saltB, pow(2, n), 256) ，（n=12)
5. The key agreement is completed, and both sides of the AB erase their session key pairs from their respective memory. (ECDH_Key,PublicKey_A,PublicKey_B)
6. Sessions following, both sides of the AB encrypt and decrypt messages using 256bit Shared_key and AES-256-GCM algorithm.
7. The negotiated key is updated when a new session is created each time.
The encrypted communication channel established by the above process ensures that communication contents are not leaked under the condition that the network flow is completely monitored. Even if the private key of server-side is mastered by the monitor, the monitor can't decrypt the actual communication contents () according to the private key of server and all network flows, and even can't know the person who is logged in or the side to whom the messages are sent. So this solution has the nature of forward secrecy because the session key pair will be erased by both sides from their respective memories after completion of negotiation.


## Document
* Connnect API  [Ducument](https://www.connect.im/developer)
* Connnect  [protocol](https://www.connect.im/developer)
* Connnect  [ptotobuf](https://github.com/connectim/protos)


## How to use?

## Referencing
* [Protobuf](https://github.com/google/protobuf)
* [Butterknife](https://github.com/JakeWharton/butterknife)
* [EventBus](https://github.com/greenrobot/EventBus)
* [GreenDAO](https://github.com/greenrobot/greenDAO) 
* [Gson](https://github.com/google/gson) google Gson
* [Android-Database-Sqlcipher](https://github.com/sqlcipher/android-database-sqlcipher) 
* [LibPhoneNumber](https://github.com/googlei18n/libphonenumber)
* [Junit4](https://github.com/junit-team/junit4) 
* [Glide](https://github.com/bumptech/glide)
* [Okhttp](https://github.com/square/okhttp)

## License

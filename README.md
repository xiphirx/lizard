# Lizard

An Android app for destiny.gg. Follow your favorite Twitch streamer and make meaningful contributions to chat, all while on the go!

## Development

There are two main modules: `app` and `core`. `app` is designated for all code that is specific to the mobile app. So any Android specific code goes here. `core` is for the core infrastructure that supports the mobile app, and in the future probably a desktop app. `core` should have no platform specific dependencies.

### Debug

To create a debug build, run 

    $ ./gradlew app:assembleDebug

afterwards you can find the APK in `app/build/outputs/apk/`. Alternatively, you can skip that and directly install the app to a connected Android device via

    $ ./gradlew app:installDebugDev     -- For devices
    $ ./gradlew app:installDebugEmu     -- For emulators

Similarly you can directly run the build on your device via

    $ ./gradlew app:runDebugDev     -- For devices
    $ ./gradlew app:runDebugEmu     -- For emulators

## License

    Copyright 2018 The Lizard Authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

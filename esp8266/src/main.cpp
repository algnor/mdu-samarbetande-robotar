#include <Arduino.h>
#include <ArduinoJson.h>
#include <motor_pins.h>

int ImageWidth = 1920;  //image resolution
int ImageHeight = 1080; //image resolution

int XVal = 0;           //coordinate of detected blob (x axis)
int YVal = 0;           //coordinate of detected blob (y axis)
int Radius = 0;         //radius of detected blob (can be used to "measure" distance if irl radius is known)

void setup() {
  Serial.begin(921600);
  Serial.setTimeout(30000);
  pinMode(MOTOR_A_DIR, OUTPUT);
  pinMode(MOTOR_B_DIR, OUTPUT);
  pinMode(MOTOR_B_POW, OUTPUT);
  pinMode(MOTOR_A_POW, OUTPUT);
  analogWriteRange(512);
  analogWriteFreq(40000);
  digitalWrite(MOTOR_A_DIR, 1);
  digitalWrite(MOTOR_B_DIR, 1);
}


void print_transmission_counter() {
    static unsigned long received_object_counter;
    Serial.println(F("\n\n\n\n"));
    for (int i = 0; i < 30; ++i) {
        Serial.write('-');
    }
    Serial.write('[');
    Serial.print(received_object_counter++);
    Serial.println(']');
}


void loop() {
    static StaticJsonDocument<256> json_doc;
    static bool led_state;

    print_transmission_counter();

    if (true) {
        const auto deser_err = deserializeJson(json_doc, Serial);
        if (deser_err) {
            Serial.print(F("Failed to deserialize, reason: \""));
            Serial.print(deser_err.c_str());
            Serial.println('"');
        } else  {
            Serial.print(F("Recevied valid json document with "));
            Serial.print(json_doc.size());
            Serial.println(F(" elements."));
            Serial.println(F("Pretty printed back at you:"));
            serializeJsonPretty(json_doc, Serial);
            Serial.println();
            
            if (json_doc["res"]) {
                ImageHeight = json_doc["res"]["height"];
                ImageWidth = json_doc["res"]["width"];
                Serial.println(ImageWidth);
                Serial.println(ImageHeight);
            }

            if (json_doc["drive"]) {
                XVal = map(json_doc["drive"]["x"], 0, ImageWidth, -512, 512);
                YVal = map(json_doc["drive"]["y"], 0, ImageHeight, -512, 512);
            }
        }
    }
    
    int AOutVal = (XVal + YVal) + sqrt(2) / 2.0;
    int BOutVal = (YVal - XVal) + sqrt(2) / 2.0;
    //Serial.println(AOutVal);
    //Serial.println(BOutVal);
    //Serial.println(XVal);
    //Serial.println(YVal);
    digitalWrite(MOTOR_A_DIR, AOutVal >= 0);
    digitalWrite(MOTOR_B_DIR, BOutVal >= 0);
    analogWrite(MOTOR_A_POW, abs(AOutVal));
    analogWrite(MOTOR_B_POW, abs(BOutVal));

}
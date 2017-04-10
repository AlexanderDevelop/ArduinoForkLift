#include <DistanceGP2Y0A21YK.h>
#include <DistanceGP2Y0A21YK_LUTs.h>
#include <SoftwareSerial.h>

DistanceGP2Y0A21YK Dist;
SoftwareSerial Bluetooth(2, 3);

#define servo1 9
#define servo2 10
#define Echo 11
#define Trigger 12

long durata = 0;
long distanza1 = 0;
int distanza2 = 0;
char comando;

void setup()
{
  pinMode(Echo, INPUT);
  pinMode(Trigger, OUTPUT);
  pinMode(servo1, OUTPUT);
  pinMode(servo2, OUTPUT);
  Dist.begin(A0);
  Serial.begin(9600);
  Bluetooth.begin(9600);
}

void loop()
{
  ControlloDistanza();
  if (distanza1 < 10)
  {
    delay(1000);
    for (int I = 0; I <= 7; I++)
    {
      MarciaAVANTI(servo1);
      MarciaINDIETRO(servo2);
    }
    delay(500);
    do
    {
      for (int I = 0; I <= 5; I++)
      {
        MarciaINDIETRO(servo1);
        STOP(servo2);
      }
      ControlloDistanza();
      delay(500);
    }
    while (distanza1 < 20);
    for (int I = 0; I <= 10; I++)
    {
      MarciaAVANTI(servo2);
      MarciaINDIETRO(servo1);
    }
  }
  ControlloDistanza2();
  if (distanza2 < 10)
  {
    delay(1000);
    for (int I = 0; I <= 7; I++)
    {
      MarciaAVANTI(servo2);
      MarciaINDIETRO(servo1);
    }
  }
  if (Bluetooth.available() > 0)
  {
    comando = Bluetooth.read();
    switch (comando)
    {
      case 'A':
        MarciaAVANTI(servo2);
        MarciaINDIETRO(servo1);
        Serial.print("Comando inserito = ");
        Serial.println(comando);
        break;

      case 'B':
        MarciaAVANTI(servo1);
        MarciaINDIETRO(servo2);
        Serial.print("Comando inserito = ");
        Serial.println(comando);
        break;

      case 'C':
        MarciaINDIETRO(servo1);
        STOP(servo2);
        Serial.print("Comando inserito = ");
        Serial.println(comando);
        break;

      case 'D':
        MarciaAVANTI(servo2);
        STOP(servo1);
        Serial.print("Comando inserito = ");
        Serial.println(comando);
        break;

      case 'E':
        MarciaAVANTI(servo1);
        STOP(servo2);
        Serial.print("Comando inserito = ");
        Serial.println(comando);
        break;

      case 'F':
        MarciaINDIETRO(servo2);
        STOP(servo1);
        Serial.print("Comando inserito = ");
        Serial.println(comando);
        break;
    }
  }
  else
  {
    STOP(servo1);
    STOP(servo2);
    Serial.println("STOP");
  }
}

void MarciaAVANTI(int servo)
{
  digitalWrite(servo, HIGH);
  delay(0.5);
  digitalWrite(servo, LOW);
  delay(20);
}

void MarciaINDIETRO(int servo)
{
  digitalWrite(servo, HIGH);
  delay(2);
  digitalWrite(servo, LOW);
  delay(20);
}

void STOP(int servo)
{
  digitalWrite(servo, HIGH);
  delay(1.5);
  digitalWrite(servo, LOW);
  delay(20);
}

void ControlloDistanza()
{
  digitalWrite(Trigger, LOW);
  digitalWrite(Trigger, HIGH);
  delayMicroseconds(10);
  digitalWrite(Trigger, LOW);
  durata = pulseIn(Echo, HIGH);
  distanza1 = 0.034 * durata / 2;
}

void ControlloDistanza2()
{
  distanza2 = Dist.getDistanceCentimeter();
}
<<<<<<< HEAD

=======

>>>>>>> 87a8027af1a575fd285c2d7ec9134fbf059393ca

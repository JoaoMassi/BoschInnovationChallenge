/*
    Código do projeto que realizamos em parceria com a Bosch, onde fizemos uma plantação hidropônica com monitoramento Wi-Fi em tempo real utilizando sensores de temperatura, vazão e 
    de nível que mandavam os dados para o ThingsSpeak e uma aplicação Android acessavam esses dados e mostrava pelo celular.

    Utilizamos a bomba e o nível do reservatório fornecidos pela Bosch, um sensor de temperatura e um sensor de vazão.
    
    Autor: João Guilherme Alves Massi
    Projeto Bosch Innovation Challenge
*/

// =======================================================================================================================================================================================
// -- Bibliotecas auxiliares e declarações de variáveis

#include <ESP8266WiFi.h>
#include <OneWire.h>
#include <DallasTemperature.h>

// Porta do pino de sinal do DS18B20
#define ONE_WIRE_BUS 2
#define sensordefluxo 4

// Define uma instancia do oneWire para comunicacao com o sensor
OneWire oneWire(ONE_WIRE_BUS);

DallasTemperature sensors(&oneWire);
DeviceAddress sensor1;

float temperatura;
float vazao; //Variável para armazenar o valor em L/min
int contaPulso; //Variável para a quantidade de pulsos
int i = 0; //Variável para contagem

float resistencia;                          //Variável do resutado final da medição de tensão da fonte de potência
float resistencia_ma;                       //Variável usada pela medição de resistência
float resistencia_1;                        //Variável usada pela medição de resistência
float resistencia_temp;                     //Variável usada pela medição de resistência
int j = 0;                                  //Contador

float nivel;

char* ssid = "nome_da_rede";
const char* password = "senha_da_rede";

String apiKey = "sua_apiKey";
const char* server = "api.thingspeak.com";

WiFiClient client;

// ============================================================================================================================================================================================
// -- Configurações iniciais

void setup() {
  Serial.begin(115200);
  delay(10);

  pinMode(sensordefluxo, INPUT);
  attachInterrupt(digitalPinToInterrupt(sensordefluxo), incpulso, RISING); //Configura o pino 2(Interrupção 0) para trabalhar como interrupção

  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  
  // Print the IP address
  Serial.println(WiFi.localIP());
}

// ==========================================================================================================================================================================================
// -- Loop infinito

void loop() {

    monitorarTemperatura();                       
    calcularVazao();
    calcularNivel();
    
    Serial.print(temperatura);
    Serial.print("ºC");
    Serial.println("");
    Serial.print(vazao);
    Serial.print("L/min");
    Serial.println("");
    Serial.print(nivel);
    Serial.print("%");
    Serial.println("");
    delay(20000);

    //Inicia um client TCP para o envio dos dados
  if (client.connect(server,80)) {
    String postStr = apiKey;
           postStr +="&amp;field1=";
           postStr += String(temperatura);
           postStr +="&amp;field2=";
           postStr += String(vazao);
           postStr +="&amp;field3=";
           postStr += String(nivel);
           postStr += "\r\n\r\n";
 
     client.print("POST /update HTTP/1.1\n");
     client.print("Host: api.thingspeak.com\n");
     client.print("Connection: close\n");
     client.print("X-THINGSPEAKAPIKEY: "+apiKey+"\n");
     client.print("Content-Type: application/x-www-form-urlencoded\n");
     client.print("Content-Length: ");
     client.print(postStr.length());
     client.print("\n\n");
     client.print(postStr);

  }
  
  client.stop();
}

void monitorarTemperatura (void){            
           
  sensors.requestTemperatures();
  temperatura = sensors.getTempCByIndex(0);
  
}

void incpulso () {
  
  contaPulso++;
  
}

void calcularVazao() {

  contaPulso = 0;
  sei();
  delay (1000);
  cli();

  vazao = contaPulso / 5.5;
  i++;

}

void calcularNivel() {

resistencia_ma = 0;
for (j = 0; j <= 19; j++) {
  resistencia_1 = 0;
  for (i = 0; i <= 19; i++) {
    delay (2);
    resistencia_1 = resistencia_1 + analogRead(0);
  }
  if (resistencia_ma <= resistencia_1) {
    resistencia_ma = resistencia_1;
  }
}

resistencia = resistencia_ma / 20;
resistencia_ma = resistencia;

resistencia = resistencia * 1.05;

resistencia = resistencia_ma * 170;
resistencia = resistencia / (1023 - resistencia_ma);

nivel = 4500 / resistencia;

}




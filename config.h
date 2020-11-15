#include "AdafruitIO_WiFi.h"
#include <EEPROM.h>

#define WIFI_SSID "HappyHome"
#define WIFI_PASS "145789632*"

#define IO_USERNAME "kishansinghverma"
#define IO_KEY "aio_TAiz09hsQKsrVTkAhVtCzH83PIJ9"

AdafruitIO_WiFi io(IO_USERNAME, IO_KEY, WIFI_SSID, WIFI_PASS);

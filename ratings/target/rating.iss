; Имя приложения
#define   Name       "Rating"
; Версия приложения
#define   Version    "0.0.1"
; Фирма-разработчик
#define   Publisher  "Вікінги"
; Сафт фирмы разработчика
#define   URL        "https://github.com/milkavladislav/practice_TRPZ_Vikings"
; Имя исполняемого модуля
#define   ExeName    "Рейтинг.exe"

[Setup]

; Уникальный идентификатор приложения, 
;сгенерированный через Tools -> Generate GUID
AppId={{A55046D8-05FA-44DD-8F34-52D4798D2B4B}

; Прочая информация, отображаемая при установке
AppName={#Name}
AppVersion={#Version}
AppPublisher={#Publisher}
AppPublisherURL={#URL}
AppSupportURL={#URL}
AppUpdatesURL={#URL}

; Путь установки по-умолчанию
DefaultDirName={pf}\{#Name}
; Имя группы в меню "Пуск"
DefaultGroupName={#Name}

; Каталог, куда будет записан собранный setup и имя исполняемого файла
OutputDir=C:\Users\COLDBEATZ\workspace\ratings\target
OutputBaseFileName=rating-setup

; Файл иконки
SetupIconFile=C:\Users\COLDBEATZ\workspace\ratings\src\main\resources\icon.ico

; Параметры сжатия
Compression=lzma
SolidCompression=yes

UsePreviousAppDir=no

[Languages]
;Name: "english"; MessagesFile: "compiler:Default.isl";
Name: "ukrainian"; MessagesFile: "compiler:Languages\Ukrainian.isl";
;Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl";

[Tasks]
; Создание иконки на рабочем столе
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";

[Files]

; Исполняемый файл
Source: "C:\Users\COLDBEATZ\workspace\ratings\target\Рейтинг.exe"; DestDir: "{app}"; Flags: ignoreversion

; Прилагающиеся ресурсы
;Source: "E:\work\Mirami\Mirami\bin\Release\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]

Name: "{group}\Uninstall Rating"; Filename: "{app}\unins000.exe"
Name: "{group}\Рейтинг"; Filename: "{app}\{#ExeName}"

Name: "{commondesktop}\Рейтинг"; Filename: "{app}\{#ExeName}"; Tasks: desktopicon

[Run]
Filename: {app}\{#ExeName}; Flags: postinstall nowait skipifsilent

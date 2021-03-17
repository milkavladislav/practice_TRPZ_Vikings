; ��� ����������
#define   Name       "Rating"
; ������ ����������
#define   Version    "0.0.1"
; �����-�����������
#define   Publisher  "³����"
; ���� ����� ������������
#define   URL        "https://github.com/milkavladislav/practice_TRPZ_Vikings"
; ��� ������������ ������
#define   ExeName    "�������.exe"

[Setup]

; ���������� ������������� ����������, 
;��������������� ����� Tools -> Generate GUID
AppId={{A55046D8-05FA-44DD-8F34-52D4798D2B4B}

; ������ ����������, ������������ ��� ���������
AppName={#Name}
AppVersion={#Version}
AppPublisher={#Publisher}
AppPublisherURL={#URL}
AppSupportURL={#URL}
AppUpdatesURL={#URL}

; ���� ��������� ��-���������
DefaultDirName={pf}\{#Name}
; ��� ������ � ���� "����"
DefaultGroupName={#Name}

; �������, ���� ����� ������� ��������� setup � ��� ������������ �����
OutputDir=C:\Users\COLDBEATZ\workspace\ratings\target
OutputBaseFileName=rating-setup

; ���� ������
SetupIconFile=C:\Users\COLDBEATZ\workspace\ratings\src\main\resources\icon.ico

; ��������� ������
Compression=lzma
SolidCompression=yes

UsePreviousAppDir=no

[Languages]
;Name: "english"; MessagesFile: "compiler:Default.isl";
Name: "ukrainian"; MessagesFile: "compiler:Languages\Ukrainian.isl";
;Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl";

[Tasks]
; �������� ������ �� ������� �����
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";

[Files]

; ����������� ����
Source: "C:\Users\COLDBEATZ\workspace\ratings\target\�������.exe"; DestDir: "{app}"; Flags: ignoreversion

; ������������� �������
;Source: "C:\Users\COLDBEATZ\workspace\ratings\target\installer-resources\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]

Name: "{group}\Uninstall Rating"; Filename: "{app}\unins000.exe"
Name: "{group}\�������"; Filename: "{app}\{#ExeName}"

Name: "{commondesktop}\�������"; Filename: "{app}\{#ExeName}"; Tasks: desktopicon

[Run]
Filename: {app}\{#ExeName}; Flags: postinstall nowait skipifsilent

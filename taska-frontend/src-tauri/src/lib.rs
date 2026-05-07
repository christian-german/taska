use tauri::Manager;
use tauri_plugin_log::{Target, TargetKind};
use log;
use serde_json;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
  tauri::Builder::default()
    .plugin(tauri_plugin_notification::init())
    .plugin(tauri_plugin_deep_link::init())
    .plugin(
      tauri_plugin_log::Builder::new()
        .targets([
          Target::new(TargetKind::Stdout),
          Target::new(TargetKind::LogDir { file_name: Some("taska".to_string()) }),
          Target::new(TargetKind::Webview),
        ])
        .level(log::LevelFilter::Info)
        .build()
    )
    .setup(|app| {
      {
        let window = app.get_webview_window("main").unwrap();
        window.open_devtools();

        let app_handle = app.handle().clone();
        window.on_navigation(move |url| {
          if url.scheme() == "taska" {
            let url_string = url.to_string();
            let app_handle = app_handle.clone();
            tauri::async_runtime::spawn(async move {
              app_handle.emit("oidc-callback", url_string).unwrap();
            });
            false // bloque la navigation WebView
          } else {
            true // laisse passer les autres URLs
          }
        });
      }
      Ok(())
    })
    .run(tauri::generate_context!())
    .expect("error while running tauri application");
}

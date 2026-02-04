# BaskChart - Interactive Charts with Animation

[![Telegram Contest](https://img.shields.io/badge/Telegram-Contest-0088cc.svg)](https://t.me/contest/6)

My solution for the [Telegram Chart Contest, 2019](https://t.me/contest/6). This Android library provides smooth, interactive charts with animation support, rendered via either custom Canvas drawing or OpenGL ES for high performance.

## Donate
USDT TRC20 : THR5VLuZWn7z8iiXwJ1WK9Sg4E6Ae8qUmk
USDT ERC20 : 0x4b5fAF36e95918AD3e65bF9bd42E1FDEca93db2e
Bitcoin BTC : 1AqcSbBw1UDULszboqi8mq8jE7Kykwwncm


## Features

- **Two Rendering Modes**:
  - **CanvasChartView**: Lightweight, uses Android's Canvas API for simple, battery-friendly rendering.
  - **OpenGLChartView**: Hardware-accelerated with OpenGL ES for complex animations and large datasets.
- **Themes**: Built-in light and dark modes with easy switching.
- **Interactivity**: Pinch-to-zoom, pan gestures, and smooth animations (e.g., line drawing, data point highlights).
- **Data Support**: Handles line charts, bar charts, and custom datasets with timestamps/values.
- **No External Dependencies**: Pure Android APIs (API 21+), keeping it lightweight and contest-compliant.
- **Animation**: Fluid transitions using ValueAnimator for data loading and user interactions.

## Screenshots

### CanvasChartView

![Canvas ChartView](https://i.imgur.com/YaZu8RE.jpg)
![Canvas ChartView](https://i.imgur.com/xz6mp10.jpg)
![Canvas ChartView](https://i.imgur.com/d7xQruj.jpg)

### OpenGL ChartView

![OpenGL ChartView](https://i.imgur.com/6LMDn0D.jpg)
![OpenGL ChartView](https://i.imgur.com/tl8Z1aa.jpg)
![OpenGL ChartView](https://i.imgur.com/F00Ix6v.jpg)

## Installation

1. Clone the repo:
```
git clone https://github.com/bask0xff/BaskChart.git
```
2. Open in Android Studio.
3. Sync Gradle and run on a device/emulator (min SDK 21).

To use as a library in your project:
- Copy `src/main/java/ru/bask/chart/` to your app module.
- Add to `build.gradle (Module: app)`:
```groovy
dependencies {
   implementation 'androidx.appcompat:appcompat:1.6.1'
   // No additional deps needed
}
```
## Usage
### Basic Setup
In your layout XML:
```xml
<ru.bask.chart.CanvasChartView
    android:id="@+id/chart"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```
In your Activity/Fragment:
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CanvasChartView chart = findViewById(R.id.chart);
        chart.setData(generateSampleData());  // List of ChartDataPoint
        chart.setTheme(Theme.DARK);  // or Theme.LIGHT
        chart.animateIn();  // Start animation
    }

    private List<ChartDataPoint> generateSampleData() {
        // Example: timestamp, value
        return Arrays.asList(
            new ChartDataPoint(0, 10f),
            new ChartDataPoint(1, 20f),
            // ...
        );
    }
}
```
For OpenGL mode, replace with <ru.bask.chart.OpenGLChartView>.
## API Overview
- setData(List<ChartDataPoint> data): Load chart data.
- setTheme(Theme theme): Switch light/dark.
- enableZoom(boolean): Toggle pinch gestures.
- startAnimation(AnimationType type): Animate data entry (e.g., LINE_DRAW).
See MainActivity.java for full demo.
## Contributing
Pull requests welcome! For major changes, please open an issue first.
## License
This project is licensed under the MIT License - see the LICENSE file for details.
(If no LICENSE file exists, add one: MIT for open-source friendliness.)
## Acknowledgments
Inspired by Telegram's chart contest.
Thanks to Android Open Source Project for Canvas/OpenGL APIs.

© 2025 GitHub, Inc.


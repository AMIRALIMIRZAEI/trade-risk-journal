# Trade Risk Journal

A position-size calculator and trade journal for Android — built so I stop doing risk math in my head before every trade.

## Why

Before I built this, my "risk management" was mental math I'd sometimes get wrong or just skip entirely when a trade felt urgent. This app forces the calculation every time: put in your capital, your risk tolerance, your stop-loss, and your leverage, and it tells you the position size — no guessing, no rounding in the wrong direction.

## What's in it

**Position size calculator.** Four inputs — total capital, risk %, stop-loss %, and leverage — and it works out how much to actually put into the trade.

**Trade journal.** A log of entries and exits, so six months from now I can check whether I actually followed my own plan or just told myself I did.

## The formula

```
Risk Amount     = Capital × Risk %
Position Size   = Risk Amount ÷ Stop-Loss %
Margin Required = Position Size ÷ Leverage
```

Adjust any one input and everything downstream recalculates.

## Stack

Android, Kotlin, Gradle.

## Running it

```
git clone <repo-url>
```

Open the folder in Android Studio, let Gradle sync, hit run.

## Status

Personal tool, actively evolving — I add to it as my own trading habits change. Not financial advice, just the calculator I wish existed when I started.
🎗🪬

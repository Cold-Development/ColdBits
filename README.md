[![ColdBits](https://imgur.com/RN0PdF0.png)](https://github.com/Cold-Development/ColdBits)<br>

[![Total Downloads](https://img.shields.io/github/downloads/Cold-Development/ColdBits/total?logo=coin&color=635aa7)](https://github.com/Cold-Development/ColdBits/releases)
&nbsp;
[![Latest Release](https://img.shields.io/nexus/r/dev.padrewin/coldbits?server=https%3A%2F%2Fhub.colddev.dev%2F&logo=apache&label=Release&color=r0b07)](https://hub.colddev.dev/#browse/browse:public-releases:dev%2Fpadrewin%2Fcoldbits)
&nbsp;
[![Jenkins Build](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.colddev.dev%2Fjob%2FCold%2520Development%2Fjob%2FPremiumPoints%2F&style=flat&logo=jenkins&logoColor=white&logoSize=auto&label=Jenkins)](https://jenkins.colddev.dev/job/Cold%20Development/job/PremiumPoints/)
&nbsp;
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/Cold-Development/ColdBits/release.yml?branch=master&style=flat&logo=github&label=GitHub)](https://github.com/Cold-Development/ColdBits/actions/workflows/release.yml)

* **ColdBits** is a currency Minecraft plugin, adding the ability to manager points for every player, with SQLite and MySQL database support.<br>
* Supports **`SQLite`** and **`MySQL`** database. Default database is SQLite which can be edited later in `config.yml` file.<br>
* This plugin is based on [**`ColdDev`**](https://github.com/Cold-Development/ColdDev) plugin library.

---
## 📖 Documentation
All information is included and can be found in this repository's [**Wiki**](https://github.com/Cold-Development/ColdBits/wiki).<br>
For any questions or support, you can join our [**Discord server**](https://discord.colddev.dev). Here you can find all the support you need.<br>
![](https://imgur.com/JrI8683.png)

---
## ⚙️ Server compatibility<br>
ColdBits is compatible with Spigot and any forks of it.<br>
> [!NOTE]
> Recommending using Paper.<br>
> CraftBukkit is **NOT** and **will NOT** be supported.

---
## </> For developers
<p>
    <a href="https://github.com/Cold-Development/ColdBits/releases">
        <img alt="spigot" src="https://img.shields.io/nexus/r/dev.padrewin/coldbits?server=https%3A%2F%2Fhub.colddev.dev%2F&style=for-the-badge&logo=apachemaven&logoColor=red&label=Release&color=r0b07"/>
    </a>
</p>

ColdBits is a standalone plugin, so you will need to install it on any servers that have plugins which depend on it, and specify it as a dependency in your plugin.yml:<br>
```plugin.yml
depend:
  - ColdBits
```

### Maven:
- Repository<br>

```pom.xml
        <repository>
            <id>colddev-repo</id>
            <url>https://repo.colddev.dev/</url>
<!-- Or use <url>https://hub.colddev.dev/repository/public-releases/</url> -->
        </repository>
```
- Dependency
  - Replace `${version}` with the latest version available; example `1.0`.<br>
  
```pom.xml
        <dependency>
            <groupId>dev.padrewin</groupId>
            <artifactId>coldbits</artifactId>
            <version>${version}</version>
            <scope>provided</scope>
        </dependency>
```
![](https://raw.githubusercontent.com/mayhemantt/mayhemantt/Update/svg/Bottom.svg)

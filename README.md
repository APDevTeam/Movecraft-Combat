# Movecraft Combat Addon
![Combat](https://github.com/TylerS1066/Movecraft-Combat/actions/workflows/maven.yml/badge.svg)

Home of the code for the following features:
 - AA Directors
 - Cannon Directors
 - Contact Explosives
 - Durability Override
 - Fireball Lifespan
 - TNT Tracers
 - Damage Tracking
 - Combat Release Tracking

## Version support
The `legacy` branch is coded for 1.10.2 to 1.16.5 and Movecraft 7.x.

The `main` branch is coded for 1.14.4 to 1.16.5 and Movecraft 8.x.

## Download
Devevlopment builds can be found on the [GitHub Actions tab](https://github.com/TylerS1066/Movecraft-Combat/actions) of this repository.

Stable builds can be found on [our SpigotMC page](https://www.spigotmc.org/resources/movecraft-combat.81574/).

## Building
This plugin requires that the user setup and build their [Movecraft](https://github.com/APDevTeam/Movecraft) development environment, and then clone this into the same folder as your Movecraft development environment such that both Movecraft-Combat and Movecraft are contained in the same folder.  This plugin also requires you to build the latest version of 1.13.2 using build tools.

```
java -jar BuildTools.jar --rev 1.13.2
```

Then, run the following to build Movecraft-Combat through `maven`.
```
mvn clean install
```
Jars are located in `/target`.


## Support
[Github Issues](https://github.com/TylerS1066/Movecraft-Combat/issues)

[Discord](http://bit.ly/JoinAP-Dev)

The plugin is released here under the GNU General Public License V3. 

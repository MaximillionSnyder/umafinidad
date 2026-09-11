## Checklist de accesibilidad (release)

- [ ] `./gradlew :app:testDebugUnitTest` en verde
- [ ] `connectedDebugAndroidTest` (job `androidtest`) en verde
- [ ] Accessibility Scanner sin errores en: Compat, Top, Corredora, Elenco, Ajustes, Grupos, Ranking, Resultado
- [ ] TalkBack: agregar hijo → asignar 2 padres → ver resultado → guardar árbol → abrir árbol desde Ajustes
- [ ] Switch Access: recorrer las 5 tabs y completar una selección
- [ ] Fuente del sistema 200 % + escala "Muy grande": sin recortes de información esencial
- [ ] Los 4 temas: contraste de texto y de componentes ≥ 4.5:1 / 3:1
- [ ] Targets interactivos ≥ 48dp (Scanner)
- [ ] Nada depende solo del color (selección, rangos, errores)
- [ ] Foco visible con teclado Bluetooth; sin trampas; retorno de foco en diálogos/sheet

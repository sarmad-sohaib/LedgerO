# LedgerO - Cross device ledger management
## Why LedgerO?
The purpose of this app is to enable users to have a single app in which they can manage all their money lent or borrowed records with checks and balances. That's why we are designing this kind of application that give functionality like other applications and advances like auto-synchronization, transaction approval, and bill splits. The purpose of designing android is that android is the most widely used mobile operating system in the world.
## Features
1. Ledger creation
2. Approval for any CRUD operation in a ledger.
3. Realtime sync between ledger holders.
4. Notification for changes in ledger.
5. Reminders
6. Bio-metric login (if user has already loged in using username and password)
7. Cash register
8. Multi-language support (Englis & Urdu)
## Tools and Technologies
- Kotlin
- Firebase
- Hilt
- Architecture components
- Navigation components
- Room persistence library
- Kotlin coroutines
- Retrofit
## What is transaction approval?
The unique feature of this app is transaction aproval. This feature works when there is a change in ledger from any ledger holder. Approval system does'nt let those changes come into effect but after other ledger holder approves those changes. This mutual approval system ensures the credebility and intgrity of ledger. Any ledger of any user means that this ledger is source of truth for all transactions recorded in that ledger because ledger updates if and only if all ledger holders agree.

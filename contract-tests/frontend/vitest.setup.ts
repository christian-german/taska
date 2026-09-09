import '../../taska-frontend/node_modules/@angular/compiler/fesm2022/compiler.mjs';
import { getTestBed } from '../../taska-frontend/node_modules/@angular/core/fesm2022/testing.mjs';
import {
  BrowserTestingModule,
  platformBrowserTesting,
} from '../../taska-frontend/node_modules/@angular/platform-browser/fesm2022/testing.mjs';

getTestBed().initTestEnvironment(BrowserTestingModule, platformBrowserTesting(), {
  errorOnUnknownElements: true,
  errorOnUnknownProperties: true,
});

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {HomeComponent} from './pages/home/home.component';
import {UncertaintyComponent} from './pages/uncertainty/uncertainty.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'uncertainty/:id', component: UncertaintyComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

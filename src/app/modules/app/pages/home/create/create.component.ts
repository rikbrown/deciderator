import {AfterViewInit, Component, Directive, ElementRef, TemplateRef, ViewChild, ViewChildren} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {Uncertainty, UncertaintyOption} from '../../../../../core/services/uncertainty/types';
import {UncertaintyService} from '../../../../../core/services/uncertainty/uncertainty.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.scss'],
})
export class CreateComponent {
  uncertainty: Uncertainty = null;
  submitted = false;
  failure?: string = null;

  @ViewChild('content') private contentTmpl: TemplateRef<any>;
  private modalRef: NgbModalRef;

  @ViewChild('optionElement') someInput: ElementRef;

  constructor(
    private uncertaintyService: UncertaintyService,
    private modalService: NgbModal,
    private router: Router,
  ) { }

  get filteredOptions(): Array<string> {
    return this.uncertainty.options
      .filter((o) => o.name.trim().length > 0)
      .map((o) => o.name)
      .filter((o, i, self) => self.indexOf(o) === i);
  }

  get optionsValid(): boolean {
    return this.filteredOptions.length >= 2;
  }

  open(): void {
    this.uncertainty = {
      id: null,
      rules: null,
      name: '',
      options: [
        { name: '' },
        { name: '' },
      ]
    };

    this.modalRef = this.modalService.open(this.contentTmpl, {
      centered: true,
    });
  }

  addChoice() {
    this.uncertainty.options.push({ name: '' });
  }

  onSubmit() {
    this.failure = null;
    this.submitted = true;
    this.uncertaintyService.createUncertainty(this.uncertainty).subscribe(
      (uncertainty) => {
        this.router.navigate(['/uncertainty', uncertainty.id]).then(() => this.modalRef?.close());
      },
      (err) => {
        this.submitted = false;
        this.failure = err.toString();
      }
    );
  }
}

interface Option {
  name: string;
}
